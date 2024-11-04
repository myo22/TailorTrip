package com.tailorTrip.service;

import com.tailorTrip.domain.Itinerary;
import com.tailorTrip.domain.UserPreferences;
import com.tailorTrip.dto.ItineraryDTO;

public interface ItineraryService {
    ItineraryDTO createItinerary(UserPreferences preferences);

    void saveItinerary(ItineraryDTO itineraryDto, String userId);

    void updateItinerary(String userId, Long id, ItineraryDTO itineraryDTO);

    ItineraryDTO getItineraryById(Long id);
}
