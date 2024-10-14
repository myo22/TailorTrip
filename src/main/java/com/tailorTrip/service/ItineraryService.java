package com.tailorTrip.service;

import com.tailorTrip.domain.UserPreferences;
import com.tailorTrip.dto.Itinerary;

public interface ItineraryService {
    Itinerary createItinerary(UserPreferences preferences);
}
