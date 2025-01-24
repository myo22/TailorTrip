package com.tailorTrip.service;

import com.tailorTrip.domain.Itinerary;
import com.tailorTrip.domain.UserPreferences;
import com.tailorTrip.dto.ItineraryDTO;
import com.tailorTrip.dto.ItineraryRequestDTO;

public interface ItineraryService {
    ItineraryDTO createItinerary(UserPreferences preferences);

    void saveItinerary(ItineraryRequestDTO itineraryRequestDTO, String userId);

    void updateItinerary(String userId, Long id, ItineraryDTO itineraryDTO);

    ItineraryDTO getItineraryById(Long id);
}
