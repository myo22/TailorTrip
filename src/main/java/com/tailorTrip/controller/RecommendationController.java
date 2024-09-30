package com.tailorTrip.controller;

import com.tailorTrip.domain.Place;
import com.tailorTrip.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/recommend")
    public String recommend() {
        return "recommend";
    }

    @PostMapping("/recommend")
    public String recommend(@RequestParam String purpose,
                            @RequestParam String pace,
                            @RequestParam String transportation,
                            @RequestParam String interest,
                            Model model) {

        List<Place> recommendedPlaces = recommendationService.filterAndRecommend(purpose, pace, transportation, interest);
        model.addAttribute("places", recommendedPlaces);

        return "recommend";
    }
}
