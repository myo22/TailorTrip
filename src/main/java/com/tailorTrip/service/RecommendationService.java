package com.tailorTrip.service;


import com.tailorTrip.domain.Place;
import com.tailorTrip.domain.UserPreferences;

import java.util.Set;

public interface RecommendationService {

    Set<Place> getRecommendations(UserPreferences preferences);
}
