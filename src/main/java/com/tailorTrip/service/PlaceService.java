package com.tailorTrip.service;

import com.tailorTrip.domain.Place;

import java.util.List;

public interface PlaceService {

    List<Place> getRegionalPlaces(String region);
}
