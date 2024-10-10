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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;

    private final RecommendationService recommendationService;

    private final PlaceRepository placeRepository;

    public Itinerary createItinerary(UserPreferences preferences) {
        List<Place> recommendedPlaces = recommendationService.getRecommendations(preferences);

        List<ItineraryItem> items  = new ArrayList<>();

        // 여행 기간에 따른 일정 일수 설정
        int days = preferences.getTripDuration();

        for (int day = 1; day <= days; day++) {
            // 하루 일정에 포함될 장소 수
            int placesPerDay = 3;

            // 여행 스타일에 따른 장소 수 조정
            if (preferences.getTravelPace().equals("느긋하게")) {
                placesPerDay -= 1; // 오전 활동 1개 빼기
            } else if (preferences.getTravelPace().equals("바쁘게")) {
                placesPerDay += 1; // 저녁 활동 1개 추가
            }

            // 하루에 추천할 장소 선택
            List<Place> dailyPlaces = recommendedPlaces.stream()
                    .skip((day - 1) * placesPerDay)
                    .limit(placesPerDay)
                    .collect(Collectors.toList());

            for (Place place : dailyPlaces) {
                ItineraryItem item = ItineraryItem.builder()
                        .timeOfDay(determineTimeOfDay())
                        .activityType(determineActivityType(place))
                        .placeId(place.getId())
                        .build();
                items.add(item);
            }
        }

        Itinerary itinerary = Itinerary.builder()
                .items(items)
                .duration(days)
                .build();

        return itineraryRepository.save(itinerary);
    }

    private String determineTimeOfDay() {
        // 간단한 예시로 아침, 점심, 저녁 중 하나를 랜덤으로 선택
        String[] times = {"아침", "점심", "저녁"};
        int idx = new Random().nextInt(times.length);
        return times[idx];
    }

    private String determineActivityType(Place place) {
        // 장소의 카테고리에 따라 활동 유형 결정
        switch (place.getCat1()) {
            case "A01":
                return "관광";
            case "A02":
                return "문화 체험";
            case "A03":
                return "레포츠";
            case "B02":
                return "숙소";
            case "A04":
                return "쇼핑";
            case "A05":
                return "식사";
            default:
                return "관광";
        }
    }
}