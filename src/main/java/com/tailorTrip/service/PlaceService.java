package com.tailorTrip.service;

import com.tailorTrip.domain.Place;

import java.util.List;

public interface PlaceService {

    List<Place> getRegionalPlaces(String region);

    void updatePlaceOverview(Place place, String overview);

    void updatePlaceImage(Place place, String  imageUrl);

    void updatePlaceHubRank(Place place, String  hubRank);
}
