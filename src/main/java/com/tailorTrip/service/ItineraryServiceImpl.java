package com.tailorTrip.service;

import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.*;
import com.tailorTrip.dto.Itinerary;
import com.tailorTrip.dto.ItineraryDay;
import com.tailorTrip.dto.ItineraryItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ItineraryServiceImpl implements ItineraryService {

    private final RecommendationService recommendationService;

    @Override
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
        Place selectedAccommodation = selectAccommodation(accommodations, preferences, duration);

        // 초기 위치: 숙소가 있다면 숙소 위치, 아니면 첫 번째 장소의 위치
        double currentLat = selectedAccommodation != null ? selectedAccommodation.getMapy() : 0.0;
        double currentLng = selectedAccommodation != null ? selectedAccommodation.getMapx() : 0.0;

        List<ItineraryDay> itineraryDays = new ArrayList<>();
        int totalDays = duration;

        for (int day = 1; day <= totalDays; day++) {
            List<ItineraryItem> items = new ArrayList<>();

            // 하루에 필요한 장소 수 설정
            int mealsPerDay = 3; // 아침, 점심, 저녁
            int activitiesPerDay = 2; // 오전, 오후 활동

            if (preferences.getTravelPace().equalsIgnoreCase("느긋하게")) {
                mealsPerDay -= 1; // 점심, 저녁
                activitiesPerDay -= 1; // 느긋하게: 오후 활동
            } else if (preferences.getTravelPace().equalsIgnoreCase("바쁘게")) {
                activitiesPerDay += 1; // 바쁘게: 오전, 오후, 저녁 활동
            }

            // 식사 장소 할당 (가까운 곳으로 선택)
            for (int i = 0; i < mealsPerDay && !meals.isEmpty(); i++) {
                Place closestMeal = findClosestPlace(currentLat, currentLng, meals);
                if (closestMeal != null) {
                    items.add(ItineraryItem.builder()
                            .timeOfDay(determineMealTimeOfDay(i))
                            .place(closestMeal)
                            .activityType("식사")
                            .build());

                    // 현재 위치 업데이트
                    currentLat = closestMeal.getMapy();
                    currentLng = closestMeal.getMapx();

                    meals.remove(closestMeal); // 선택된 장소 제거
                }
            }

            // 활동 장소 할당 (가까운 곳으로 선택)
            for (int i = 0; i < activitiesPerDay && !activities.isEmpty(); i++) {
                Place closestActivity = findClosestPlace(currentLat, currentLng, activities);
                if (closestActivity != null) {
                    items.add(ItineraryItem.builder()
                            .timeOfDay(determineActivityTimeOfDay(i))
                            .place(closestActivity)
                            .activityType(determineActivityType(closestActivity))
                            .build());

                    // 현재 위치 업데이트
                    currentLat = closestActivity.getMapy();
                    currentLng = closestActivity.getMapx();

                    activities.remove(closestActivity); // 선택된 장소 제거
                }
            }

            // 마지막 날에 숙소 배정
            if (selectedAccommodation != null && day == totalDays) {
                items.add(ItineraryItem.builder()
                        .timeOfDay("숙소")
                        .place(selectedAccommodation)
                        .activityType("숙박")
                        .build());
            }

            itineraryDays.add(ItineraryDay.builder()
                    .dayNumber(day)
                    .items(items)
                    .build());
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
                return CategoryType.ACTIVITY; // 기본적으로 활동으로 분류
        }
    }

    /**
     * 선호 숙소 유형에 맞는 숙소를 선택합니다.
     *
     * @param accommodations 숙소 목록
     * @param preferences     사용자 선호도
     * @param duration        여행 기간
     * @return 선택된 숙소
     */
    private Place selectAccommodation(List<Place> accommodations, UserPreferences preferences, int duration) {
        if (accommodations.isEmpty()) return null;

        // 숙소 교대 간격 설정 (예: 3일마다 교대)
        int accommodationChangeInterval = 3;
        // 단, 전체 기간이 짧다면 한 번만 선택
        accommodationChangeInterval = Math.min(accommodationChangeInterval, duration);

        // 선호 숙소 유형에 맞는 숙소 필터링 후 평점 순으로 정렬
        List<Place> preferredAccommodations = accommodations.stream()
                .filter(place -> place.getCat3().equalsIgnoreCase(preferences.getAccommodationPreference()))
                .sorted(Comparator.comparingDouble(Place::getRating).reversed())
                .collect(Collectors.toList());

        if (!preferredAccommodations.isEmpty()) {
            return preferredAccommodations.get(0); // 평점이 가장 높은 숙소 선택
        } else {
            // 선호 숙소 유형이 없을 경우 평점이 높은 숙소 선택
            return accommodations.stream()
                    .sorted(Comparator.comparingDouble(Place::getRating).reversed())
                    .findFirst()
                    .orElse(accommodations.get(0));
        }
    }

    /**
     * 현재 위치와 가장 가까운 장소를 찾는 메서드
     *
     * @param currentLat      현재 위도
     * @param currentLng      현재 경도
     * @param availablePlaces 선택 가능한 장소 목록
     * @return 가장 가까운 장소
     */
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

    /**
     * 두 지점 간의 거리 계산 (Haversine 공식 사용)
     *
     * @param lat1 위도1
     * @param lon1 경도1
     * @param lat2 위도2
     * @param lon2 경도2
     * @return 거리 (km)
     */
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

    private String determineMealTimeOfDay(int index) {
        switch (index) {
            case 0:
                return "아침";
            case 1:
                return "점심";
            case 2:
                return "저녁";
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

    private String determineActivityType(Place place) {
        // 장소의 카테고리에 따라 활동 유형 결정
        switch (place.getCat1()) {
            case "A01":
                return "자연 관광";
            case "A02":
                return "문화/역사 탐방";
            case "A03":
                return "레포츠";
            default:
                return "관광";
        }
    }

    private enum CategoryType {
        MEAL, ACTIVITY, ACCOMMODATION
    }
}