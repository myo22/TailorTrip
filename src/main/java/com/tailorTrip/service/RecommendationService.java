package com.tailorTrip.service;

import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.Place;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class RecommendationService {

    private final PlaceRepository placeRepository;

    private final DirectionsService directionsService;

    public List<Place> filterAndRecommend(String purpose, String pace, String transportation, String interest) {
        // 사용자의 관심사와 여행 속도에 맞춰 장소 필터링
        List<Place> filteredPlaces = placeRepository.findAll().stream()
                .filter(place -> matchesInterest(place, interest))
                .filter(place -> matchesPaceAndTransportation(place, pace, transportation))
                .collect(Collectors.toList());

        log.info("Filtered places count: " + filteredPlaces.size());

        // Google Directions API를 사용하여 최적화된 경로 생성
        List<Place> optimizedRoute = directionsService.optimizeRoute(filteredPlaces, transportation);

        log.info("Optimized route size: " + optimizedRoute.size());

        if (optimizedRoute.isEmpty()) {
            log.warn("Optimized route is empty.");
            return Collections.emptyList();
        }

        // 예시 2: 별점 높은 순으로 정렬된 경로
        List<Place> highRatingRoute = optimizedRoute.stream()
                .sorted(Comparator.comparingDouble(Place::getRating).reversed())
                .collect(Collectors.toList());


        log.info("Optimized route size: " + highRatingRoute.size());

        return highRatingRoute;

    }

    private boolean matchesInterest(Place place, String interest) {
        return place.getTypes().stream().anyMatch(type -> type.equalsIgnoreCase(interest));
    }

    private boolean matchesPaceAndTransportation(Place place, String pace, String transportation) {
        if (transportation.equalsIgnoreCase("걸어서")) {
            return place.isWalkable();
        } else if (transportation.equalsIgnoreCase("차타고")) {
            return place.isFastAccess();
        }
        return false;
    }


}
