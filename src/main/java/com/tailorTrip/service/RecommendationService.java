package com.tailorTrip.service;

import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final PlaceRepository placeRepository;

    private final DirectionsService directionsService;

    public List<Place> filterAndRecommend(String purpose, String pace, String transportation, String interest) {
        // 사용자 입력을 기반으로 장소 필터링
        List<Place> filteredPlaces = placeRepository.findAll().stream()
                .filter(place -> matchesInterest(place, interest))
                .filter(place -> matchesPace(place, pace))
                .collect(Collectors.toList());

        // 경로 최적화
        List<Place> optimizedRoute = directionsService.optimizeRoute(filteredPlaces, transportation);

        return optimizedRoute;

    }

    private boolean matchesInterest(Place place, String interest) {
        return place.getCategory().equalsIgnoreCase(interest);
    }

    private boolean matchesPace(Place place, String pace) {
        return pace.equalsIgnoreCase("느긋하게") ? place.isWalkable() : place.isFastAccess();
    }

}
