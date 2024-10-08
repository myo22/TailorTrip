package com.tailorTrip.service;

import com.tailorTrip.Repository.ItineraryRepository;
import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.Itinerary;
import com.tailorTrip.domain.ItineraryItem;
import com.tailorTrip.domain.Place;
import com.tailorTrip.domain.UserPreferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;

    private final RecommendationService recommendationService;

    private final PlaceRepository placeRepository;

    public Itinerary createItinerary(UserPreferences preferences) {
        List<Place> recommendedPlaces = recommendationService.getRecommendations(preferences);

        List<ItineraryItem> items  = new ArrayList<>();

        // 여행 스타일에 따른 활동 조정
        boolean isRelaxed = preferences.getPace().equals("느긋하게");

        // 숙박 일수
        int duration = preferences.getDuration();

        // 하루 코스 예시
        if (duration >= 1) {
            // 아침 식사
            Place breakfastPlace = selectPlace(recommendedPlaces, "cafe");
            if (breakfastPlace != null) {
                items.add(ItineraryItem.builder()
                        .timeOfDay("아침")
                        .activityType("식사")
                        .placeId(breakfastPlace.getId())
                        .build());
            }

            // 오전 활동 (관광지)
            Place morningActivity = selectPlace(recommendedPlaces, "tourist attraction");
            if (morningActivity != null) {
                items.add(ItineraryItem.builder()
                        .timeOfDay("오전")
                        .activityType("관광")
                        .placeId(morningActivity.getId())
                        .build());
            }

            // 점심 식사
            Place lunchPlace = selectPlace(recommendedPlaces, "restaurant");
            if (lunchPlace != null) {
                items.add(ItineraryItem.builder()
                        .timeOfDay("점심")
                        .activityType("식사")
                        .placeId(lunchPlace.getId())
                        .build());
            }

            // 오후 활동 (쇼핑 또는 산책)
            Place afternoonActivity = isRelaxed ? selectPlace(recommendedPlaces, "shopping") : selectPlace(recommendedPlaces, "walking");
            if (afternoonActivity != null) {
                items.add(ItineraryItem.builder()
                        .timeOfDay("오후")
                        .activityType(isRelaxed ? "쇼핑" : "산책")
                        .placeId(afternoonActivity.getId())
                        .build());
            }

            // 저녁 식사
            Place dinnerPlace = selectPlace(recommendedPlaces, "restaurant");
            if (dinnerPlace != null) {
                items.add(ItineraryItem.builder()
                        .timeOfDay("저녁")
                        .activityType("식사")
                        .placeId(dinnerPlace.getId())
                        .build());
            }

            // 숙소 (1박 이상인 경우)
            if (duration > 1) {
                Place accommodation = selectPlace(recommendedPlaces, "hotel");
                if (accommodation != null) {
                    items.add(ItineraryItem.builder()
                            .timeOfDay("숙소")
                            .activityType("숙박")
                            .placeId(accommodation.getId())
                            .build());
                }
            }
        }

        Itinerary itinerary = Itinerary.builder()
                .items(items)
                .duration(duration)
                .build();

        return itineraryRepository.save(itinerary);
    }


    private Place selectPlace(List<Place> places, String category) {
        return places.stream()
                .filter(place -> place.getCat1().equalsIgnoreCase(category) ||
                        place.getCat2().equalsIgnoreCase(category) ||
                        place.getCat3().equalsIgnoreCase(category))
                .sorted(Comparator.comparingDouble(Place::getRating).reversed()
                        .thenComparingInt(Place::getUserRatingsTotal).reversed())
                .findFirst()
                .orElse(null);
    }

    public Place getPlaceById(Long placeId) {
        return placeRepository.findById(placeId).orElse(null);
    }
}
