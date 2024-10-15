package com.tailorTrip.service;

import com.tailorTrip.domain.Place;

public interface GooglePlacesService {

    public Place enrichPlaceWithDetails(Place place);

}
