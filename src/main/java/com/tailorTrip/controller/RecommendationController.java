package com.tailorTrip.controller;

import com.tailorTrip.Repository.UserPreferencesRepository;
import com.tailorTrip.domain.Itinerary;
import com.tailorTrip.domain.Place;
import com.tailorTrip.domain.UserPreferences;
import com.tailorTrip.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/travel")
@RequiredArgsConstructor
@Log4j2
public class RecommendationController {

    private final RecommendationService recommendationService;

    private final UserPreferencesRepository userPreferencesRepository;

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

        // 추천 일정 가져오기
        List<Itinerary> recommendedItineraries = recommendationService.generateItineraries(prefs);

        model.addAttribute("itineraries", recommendedItineraries);

        return "recommendResult";
    }
}
