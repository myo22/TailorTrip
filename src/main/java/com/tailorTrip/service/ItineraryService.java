package com.tailorTrip.service;

import com.tailorTrip.domain.UserPreferences;
import com.tailorTrip.dto.ItineraryDTO;

public interface ItineraryService {
    ItineraryDTO createItinerary(UserPreferences preferences);

    void saveItinerary(ItineraryDTO itinerary, String userId);
}
