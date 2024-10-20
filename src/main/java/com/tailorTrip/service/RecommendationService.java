package com.tailorTrip.service;


import com.tailorTrip.domain.Place;
import com.tailorTrip.domain.UserPreferences;

import java.util.List;

public interface RecommendationService {

    List<Place> getRecommendations(UserPreferences preferences);
}
