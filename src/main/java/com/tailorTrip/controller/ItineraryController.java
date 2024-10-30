package com.tailorTrip.controller;

import com.tailorTrip.domain.Itinerary;
import com.tailorTrip.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Log4j2
public class ItineraryController {

    private final ItineraryService itineraryService;

    @PostMapping("/save")
    public ResponseEntity<String> saveItinerary(@RequestBody Itinerary itinerary, Principal principal) {
        String userId = principal.getName(); // 현재 로그인한 사용자 ID
//        itineraryService.saveItinerary(userId, itinerary);
        return ResponseEntity.ok("Itinerary saved successfully!");
    }
}
