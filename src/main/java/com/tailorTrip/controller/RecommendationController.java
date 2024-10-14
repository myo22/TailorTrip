package com.tailorTrip.controller;

import com.tailorTrip.Repository.UserPreferencesRepository;
import com.tailorTrip.dto.Itinerary;
import com.tailorTrip.domain.Place;
import com.tailorTrip.domain.UserPreferences;
import com.tailorTrip.service.ItineraryService;
import com.tailorTrip.service.RecommendationService;
import com.tailorTrip.service.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

    private final RegionService regionService;

    @GetMapping("/recommend")
    public String getRecommendationPage() {
        return "recommend";
    }

    @PostMapping("/recommend")
    public String recommend(
            @RequestParam String region,
            @RequestParam String interest,
            @RequestParam String activityType,
            @RequestParam boolean petFriendly,
            @RequestParam String foodPreference,
            @RequestParam String travelPace,
            @RequestParam String accommodationPreference,
            @RequestParam int tripDuration,
            Model model) {

        // 현재 로그인한 사용자 찾기
//        String username = authentication.getName();
//        Member member = memberRepository.findById(username).orElseThrow(() -> new RuntimeException("User not found"));


        // 지역에 따른 중심 좌표 가져오기
        double[] coordinates = regionService.getCoordinates(region);
        double userLat = coordinates[0];
        double userLng = coordinates[1];


        // 사용자 선호도 객체 생성
        UserPreferences prefs = UserPreferences.builder()
                .region(region)
                .interest(interest)
                .activityType(activityType)
                .petFriendly(petFriendly)
                .foodPreference(foodPreference)
                .travelPace(travelPace)
                .accommodationPreference(accommodationPreference)
                .tripDuration(tripDuration)
                .userLat(userLat)
                .userLng(userLng)
                .build();

        // 사용자 선호도 저장
        userPreferencesRepository.save(prefs);

        Itinerary itinerary = itineraryService.createItinerary(prefs);

        // 모델에 일정 추가
        model.addAttribute("itinerary", itinerary);

        return "recommendResult";
    }
}
