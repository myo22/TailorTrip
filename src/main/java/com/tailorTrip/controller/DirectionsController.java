package com.tailorTrip.controller;

import com.google.maps.model.DirectionsResult;
import com.tailorTrip.service.GoogleMapsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DirectionsController {

    private final GoogleMapsService googleMapsService;

    public DirectionsController(GoogleMapsService googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    @GetMapping("/directions")
    public DirectionsResult getDirections(
            @RequestParam String origin,
            @RequestParam String destination) throws Exception {
        return googleMapsService.getDirections(origin, destination);
    }
}
