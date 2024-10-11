package com.tailorTrip.service;

import com.tailorTrip.domain.*;
import com.tailorTrip.dto.Itinerary;
import com.tailorTrip.dto.ItineraryDay;
import com.tailorTrip.dto.ItineraryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final RecommendationService recommendationService;


    public Itinerary createItinerary(UserPreferences preferences) {
        int duration = preferences.getTripDuration(); // 여행 기간
        List<Place> recommendedPlaces = recommendationService.getRecommendations(preferences); // 100개의 장소들

        // 장소를 카테고리별로 분류
        Map<CategoryType, List<Place>> categorizedPlaces = categorizePlaces(recommendedPlaces);

        // 숙소는 별도로 관리
        List<Place> accommodations = categorizedPlaces.getOrDefault(CategoryType.ACCOMMODATION, new ArrayList<>());

        // 식사 및 활동 장소
        List<Place> meals = categorizedPlaces.getOrDefault(CategoryType.MEAL, new ArrayList<>());
        List<Place> activities = categorizedPlaces.getOrDefault(CategoryType.ACTIVITY, new ArrayList<>());

        List<ItineraryDay> itineraryDays = new ArrayList<>();
        int totalDays = duration;

        // 숙소 선택 (최대 1개)
        Place selectedAccommodation = selectAccommodation(accommodations, preferences);


        for (int day = 1; day <= totalDays; day++) {
            List<ItineraryItem> items = new ArrayList<>();

            // 하루에 필요한 장소 수 설정
            int mealsPerDay = 3; // 아침, 점심, 저녁
            int activitiesPerDay = 2; // 오전, 오후 활동

            if (preferences.getTravelPace().equals("느긋하게")) {
                mealsPerDay -= 1; // 점심, 저녁
                activitiesPerDay -= 1; // 느긋하게: 오후 활동
            } else if (preferences.getTravelPace().equals("바쁘게")) {
                activitiesPerDay += 1; // 바쁘게: 오전, 오후, 저녁 활동
            }

            // 식사 장소 할당
            List<Place> dayMeals = selectPlaces(meals, mealsPerDay);
            for (int i = 0; i < dayMeals.size(); i++) {
                Place place = dayMeals.get(i);
                String timeOfDay = determineMealTimeOfDay(i);
                ItineraryItem item = ItineraryItem.builder()
                        .timeOfDay(timeOfDay)
                        .place(place)
                        .activityType("식사")
                        .build();
                items.add(item);
            }

            // 활동 장소 할당
            List<Place> dayActivities = selectPlaces(activities, activitiesPerDay);
            for (int i = 0; i < dayActivities.size(); i++) {
                Place place = dayActivities.get(i);
                String timeOfDay = determineActivityTimeOfDay(i);
                ItineraryItem item = ItineraryItem.builder()
                        .timeOfDay(timeOfDay)
                        .place(place)
                        .activityType("활동")
                        .build();
                items.add(item);
            }

            // 숙소 배정 (마지막 날 제외)
            if (selectedAccommodation != null && day == totalDays) {
                ItineraryItem accommodationItem = ItineraryItem.builder()
                        .timeOfDay("숙소")
                        .place(selectedAccommodation)
                        .activityType("숙박")
                        .build();
                items.add(accommodationItem);
            }

            ItineraryDay itineraryDay = ItineraryDay.builder()
                    .dayNumber(day)
                    .items(items)
                    .build();

            itineraryDays.add(itineraryDay);
        }

        return Itinerary.builder()
                .duration(duration)
                .days(itineraryDays)
                .build();
    }

    private Map<CategoryType, List<Place>> categorizePlaces(List<Place> places) {
        Map<CategoryType, List<Place>> categorized = new HashMap<>();
        for (Place place : places) {
            CategoryType type = determineCategoryType(place);
            categorized.computeIfAbsent(type, k -> new ArrayList<>()).add(place);
        }
        return categorized;
    }

    private CategoryType determineCategoryType(Place place) {
        switch (place.getCat1()) {
            case "A05":
                return CategoryType.MEAL;
            case "B02":
                return CategoryType.ACCOMMODATION;
            default:
                return CategoryType.ACTIVITY;
        }
    }

    private List<Place> selectPlaces(List<Place> availablePlaces, int count) {
        List<Place> selected = new ArrayList<>();
        Iterator<Place> iterator = availablePlaces.iterator();
        while (iterator.hasNext() && selected.size() < count) {
            selected.add(iterator.next());
            iterator.remove();
        }
        return selected;
    }

    // 아래와 같이 추가적인 필터링 단계를 거치는 것은 모델의 추천 정확성 보장, 데이터 일관성 및 품질 확보, 사용자 선호도의 세부 조정 등이 있다.
    private Place selectAccommodation(List<Place> accommodations, UserPreferences preferences) {
        if (accommodations.isEmpty()) return null;
        // 숙소는 선호 숙소 유형에 맞추어 선택
        return accommodations.stream()
                .filter(place -> place.getCat3().equals(preferences.getAccommodationPreference()))
                .findFirst()
                .orElse(accommodations.get(0));
    }

    private String determineMealTimeOfDay(int index) {
        switch (index) {
            case 0:
                return "저녁";
            case 1:
                return "점심";
            case 2:
                return "아침";
            default:
                return "식사";
        }
    }

    private String determineActivityTimeOfDay(int index) {
        switch (index) {
            case 0:
                return "오전 활동";
            case 1:
                return "오후 활동";
            case 2:
                return "저녁 활동";
            case 3:
                return "밤 활동";
            default:
                return "활동";
        }
    }

//    이 코드는 미래 확장성을 고려한 것이고, 활동 유형별로 다른 처리가 필요해진다면 다시 사용하면 된다.
//    private String determineActivityType(Place place) {
//        // 장소의 카테고리에 따라 활동 유형 결정
//        switch (place.getCat1()) {
//            case "A01":
//                return "자연 관광";
//            case "A02":
//                return "문화/역사 탐방";
//            case "A03":
//                return "레포츠";
//            default:
//                return "관광";
//        }
//    }

    private enum CategoryType {
        MEAL, ACTIVITY, ACCOMMODATION
    }
}