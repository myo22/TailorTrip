package com.tailorTrip.controller;

import com.google.maps.model.PlacesSearchResponse;
import com.tailorTrip.service.GooglePlaceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlacesController {

    private final GooglePlaceService googlePlaceService;

    public PlacesController(GooglePlaceService googlePlaceService) {
        this.googlePlaceService = googlePlaceService;
    }

    @GetMapping("/places")
    public PlacesSearchResponse searchPlaces(
            @RequestParam String query,
            @RequestParam String lat,
            @RequestParam String lng) throws Exception {
        return googlePlaceService.searchPlaces(query, lat, lng);
        }
}
