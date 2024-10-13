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

        // 숙소 선택 (최대 1개)
        Place selectedAccommodation = selectAccommodation(accommodations, preferences, 1, duration);

        // 초기 위치: 숙소가 있다면 숙소 위치, 아니면 첫 번째 장소의 위치
        double currentLat = selectedAccommodation != null ? selectedAccommodation.getMapy() : (meals.isEmpty() ? 0.0 : meals.get(0).getMapy());
        double currentLng = selectedAccommodation != null ? selectedAccommodation.getMapx() : (meals.isEmpty() ? 0.0 : meals.get(0).getMapx());

        List<ItineraryDay> itineraryDays = new ArrayList<>();
        int totalDays = duration;

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
            for (int i = 0; i < mealsPerDay; i++) {
                Place meal = findClosestPlace(currentLat, currentLng, meals);
                if (meal != null) {
                    String timeOfDay = determineMealTimeOfDay(i);
                    ItineraryItem item = ItineraryItem.builder()
                            .timeOfDay(timeOfDay)
                            .place(meal)
                            .activityType("식사")
                            .build();
                    items.add(item);
                    currentLat = meal.getMapy();
                    currentLng = meal.getMapx();
                    meals.remove(meal); // 선택된 장소는 리스트에서 제거
                }
            }


            // 활동 장소 할당
            for (int i = 0; i < activitiesPerDay; i++) {
                Place activity = findClosestPlace(currentLat, currentLng, activities);
                if (activity != null) {
                    String timeOfDay = determineActivityTimeOfDay(i);
                    ItineraryItem item = ItineraryItem.builder()
                            .timeOfDay(timeOfDay)
                            .place(activity)
                            .activityType("활동")
                            .build();
                    items.add(item);
                    currentLat = activity.getMapy();
                    currentLng = activity.getMapx();
                    activities.remove(activity); // 선택된 장소는 리스트에서 제거
                }
            }


            // 숙소 배정 (마지막 날)
            if (selectedAccommodation != null && day == totalDays) {
                ItineraryItem accommodationItem = ItineraryItem.builder()
                        .timeOfDay("숙소")
                        .place(selectedAccommodation)
                        .activityType("숙박")
                        .build();
                items.add(accommodationItem);
                // 숙소 위치로 현재 위치 업데이트 (필요 시)
                currentLat = selectedAccommodation.getMapy();
                currentLng = selectedAccommodation.getMapx();
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

    // 현재 위치와 가장 가까운 장소를 찾는 메서드
    private Place findClosestPlace(double currentLat, double currentLng, List<Place> availablePlaces) {
        Place closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Place place : availablePlaces) {
            double distance = calculateDistance(currentLat, currentLng, place.getMapy(), place.getMapx());
            if (distance < minDistance) {
                minDistance = distance;
                closest = place;
            }
        }

        return closest;
    }

    // 두 지점 간의 거리 계산 (Haversine 공식 사용)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반지름 (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // 거리 (km)
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
    private Place selectAccommodation(List<Place> accommodations, UserPreferences preferences, int day, int totalDays) {
        if (accommodations.isEmpty()) return null;

        // 숙소 교대 간격 설정 (예: 3일마다 교대)
        int accommodationChangeInterval = 3;
        if (day % accommodationChangeInterval == 0) {
            return accommodations.stream()
                    .filter(place -> place.getCat3().equals(preferences.getAccommodationPreference()))
                    .findFirst()
                    .orElse(accommodations.stream()
                            .sorted(Comparator.comparingDouble(Place::getRating).reversed())
                            .findFirst()
                            .orElse(accommodations.get(0)));
        }

        return null; // 교대할 숙소가 아니면 null 반환
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