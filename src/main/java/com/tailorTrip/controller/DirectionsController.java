package com.tailorTrip.controller;

import com.google.maps.model.DirectionsResult;
import com.tailorTrip.service.GooglePlacesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DirectionsController {

    private final GooglePlacesService googlePlacesService;

    public DirectionsController(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    @GetMapping("/directions")
    public DirectionsResult getDirections(
            @RequestParam String origin,
            @RequestParam String destination) throws Exception {
        return googlePlacesService.getDirections(origin, destination);
    }
}
