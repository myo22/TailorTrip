package com.tailorTrip.service;

import com.tailorTrip.domain.Place;

import java.util.concurrent.CompletableFuture;

public interface GooglePlacesService {

    public Place enrichPlaceWithDetails(Place place);
    CompletableFuture<Place> enrichPlaceWithDetailsAsync(Place place);
}
