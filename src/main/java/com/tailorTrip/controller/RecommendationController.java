package com.tailorTrip.controller;

import com.tailorTrip.domain.Place;
import com.tailorTrip.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        log.info("User input - Purpose: {}, Pace: {}, Transportation: {}, Interest: {}", purpose, pace, transportation, interest);

        List<Place> recommendedPlaces = recommendationService.filterAndRecommend(purpose, pace, transportation, interest);
        model.addAttribute("places", recommendedPlaces);


        log.info("Number of recommended places: {}", recommendedPlaces.size());

        return "recommendResult";
    }
}
