package com.tailorTrip.controller;

import com.tailorTrip.Repository.MemberRepository;
import com.tailorTrip.Repository.UserPreferencesRepository;
import com.tailorTrip.domain.Itinerary;
import com.tailorTrip.domain.Member;
import com.tailorTrip.domain.Place;
import com.tailorTrip.domain.UserPreferences;
import com.tailorTrip.dto.ItineraryItemDTO;
import com.tailorTrip.service.ItineraryService;
import com.tailorTrip.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
public class RecommendationController {

    private final UserPreferencesRepository userPreferencesRepository;

    private final ItineraryService itineraryService;

    @GetMapping("/recommend")
    public String getRecommendationPage() {
        return "recommend";
    }

    @PostMapping("/recommend")
    public String recommend(
            @RequestParam String purpose,
            @RequestParam String pace,
            @RequestParam String transportation,
            @RequestParam String interest,
            @RequestParam String duration,
            @RequestParam String budget,
            Model model) {

        // 현재 로그인한 사용자 찾기
//        String username = authentication.getName();
//        Member member = memberRepository.findById(username).orElseThrow(() -> new RuntimeException("User not found"));

        // 사용자 선호도 객체 생성
        UserPreferences prefs = UserPreferences.builder()
                .purpose(purpose)
                .pace(pace)
                .transportation(transportation)
                .interest(interest)
                .duration(duration)
                .budget(budget)
                .build();

        // 사용자 선호도 저장
        userPreferencesRepository.save(prefs);

        // 일정 생성
        Itinerary itinerary = itineraryService.createItinerary(prefs);


        // 일정 항목을 장소 객체로 변환
        List<ItineraryItemDTO> itineraryItems = itinerary.getItems().stream()
                .map(item -> {
                    Place place = itineraryService.getPlaceById(item.getPlaceId());
                    return new ItineraryItemDTO(item.getTimeOfDay(), item.getActivityType(), place);
                })
                .collect(java.util.stream.Collectors.toList());

        // 모델에 일정 추가
        model.addAttribute("itinerary", itineraryItems);

        return "recommendResult";
    }
}
