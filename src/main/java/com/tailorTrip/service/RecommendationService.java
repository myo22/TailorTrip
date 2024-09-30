package com.tailorTrip.service;

import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final PlaceRepository placeRepository;

    private final DirectionsService directionsService;

    public List<List<Place>> getRecommendsRoutes(String purpose, String pace, String transportation, String interest) {
        // 사용자 입력을 기반으로 장소 필터링
        List<Place> filteredPlaces = placeRepository.findAll().stream()
                .filter(place -> matchesInterest(place, interest))
                .filter(place -> matchesPace(place, pace))
                .collect(Collectors.toList());

        // Google Directions API를 사용하여 최적화된 경로 생성
        List<Place> optimizedRoute = directionsService.optimizeRoute(filteredPlaces, transportation);


        // 경로를 세 가지로 분류 (예: 가장 짧은 경로, 평점 높은 경로, 인기 있는 경로 등)
        List<List<Place>> recommendedRoutes = new ArrayList<>();;

        // 예시 1: 전체 최적화된 경로
        recommendedRoutes.add(optimizedRoute);

        // 예시 2: 별점 높은 순으로 정렬된 경로
        List<Place> highRatingRoute = optimizedRoute.stream()
                .sorted(Comparator.comparingDouble(Place::getRating).reversed())
                .collect(Collectors.toList());
        recommendedRoutes.add(highRatingRoute);

        // 예시 3: 리뷰 수 많은 순으로 정렬된 경로
        List<Place> highReviewRoute = optimizedRoute.stream()
                .sorted(Comparator.comparingInt(Place::getUserRatingsTotal).reversed())
                .collect(Collectors.toList());
        recommendedRoutes.add(highReviewRoute);

        return recommendedRoutes;

    }

    private boolean matchesInterest(Place place, String interest) {
        return place.getCategory().equalsIgnoreCase(interest);
    }

    private boolean matchesPace(Place place, String pace) {
        return pace.equalsIgnoreCase("느긋하게") ? place.isWalkable() : place.isFastAccess();
    }

}
