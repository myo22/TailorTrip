package com.tailorTrip.service;

import com.tailorTrip.domain.*;
import com.tailorTrip.dto.Itinerary;
import com.tailorTrip.dto.ItineraryDay;
import com.tailorTrip.dto.ItineraryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final RecommendationService recommendationService;


    public Itinerary createItinerary(UserPreferences preferences) {
        int duration = preferences.getTripDuration(); // 여행 기간
        List<Place> recommendedPlaces = recommendationService.getRecommendations(preferences); // 100개의 장소들


        List<ItineraryDay> itineraryDays = new ArrayList<>();
        int totalDays = duration;

        int index = 0;
        Place lastPlace = null; // 이전에 배정된 장소


        for (int day = 1; day <= totalDays; day++) {
            List<ItineraryItem> items = new ArrayList<>();

            // 하루에 필요한 장소 수 설정
            int placesPerDay = 5; // 아침, 오전 활동, 점심, 오후 활동, 저녁 (기본 5개 중 중복 제거)

            if (preferences.getTravelPace().equals("느긋하게")) {
                placesPerDay -= 2; // 느긋하게: 3곳
            } else if (preferences.getTravelPace().equals("바쁘게")) {
                placesPerDay += 1; // 바쁘게: 6곳
            }

            List<Place> dayPlaces = selectOptimalPlaces(recommendedPlaces, placesPerDay, lastPlace);

            for (int i = 0; i < dayPlaces.size(); i++) {
                Place place = dayPlaces.get(i);

                String timeOfDay = determineTimeOfDay(i);
                String activityType = determineActivityType(place);

                ItineraryItem item = ItineraryItem.builder()
                        .timeOfDay(timeOfDay)
                        .place(place)
                        .activityType(activityType)
                        .build();

                items.add(item);
                lastPlace = place; // 다음 장소 선택 시 기준으로 사용
            }

            // 숙소 배정 (1박 이상의 일정)
            if (day < totalDays) {
                Place accommodation = selectAccommodation(recommendedPlaces, preferences);
                ItineraryItem accommodationItem = ItineraryItem.builder()
                        .timeOfDay("숙소")
                        .place(accommodation)
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

    private List<Place> selectOptimalPlaces(List<Place> recommendedPlaces, int placesPerDay, Place lastPlace) {
        List<Place> selectedPlaces = new ArrayList<>();
        List<Place> availablePlaces = new ArrayList<>(recommendedPlaces);

        // 첫 장소 선택
        if (lastPlace == null && !availablePlaces.isEmpty()) {
            Place firstPlace = availablePlaces.remove(0);
            selectedPlaces.add(firstPlace);
        } else if (lastPlace != null) {
            // 마지막 장소에서 가장 가까운 장소 선택
            Place nearestPlace = findNearestPlace(lastPlace, availablePlaces);
            if (nearestPlace != null) {
                selectedPlaces.add(nearestPlace);
                availablePlaces.remove(nearestPlace);
            }
        }

        // 나머지 장소 선택
        for (int i = 1; i < placesPerDay; i++) {
            if (availablePlaces.isEmpty()) break;

            Place last = selectedPlaces.get(selectedPlaces.size() - 1);
            Place nextPlace = findNearestPlace(last, availablePlaces);
            if (nextPlace != null) {
                selectedPlaces.add(nextPlace);
                availablePlaces.remove(nextPlace);
            }
        }

        return selectedPlaces;
    }

    private Place findNearestPlace(Place current, List<Place> availablePlaces) {
        Place nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Place place : availablePlaces) {
            double distance = calculateDistance(current.getMapy(), current.getMapx(), place.getMapy(), place.getMapx());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = place;
            }
        }

        return nearest;
    }

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

    private String determineTimeOfDay(int placeIndex) {
        switch (placeIndex) {
            case 0:
                return "아침";
            case 1:
                return "오전 활동";
            case 2:
                return "점심";
            case 3:
                return "오후 활동";
            case 4:
                return "저녁";
            default:
                return "오후 활동";
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
            case "B02":
                return "숙박";
            case "A04":
                return "쇼핑";
            case "A05":
                return "식사";
            default:
                return "관광";
        }
    }

    private Place selectAccommodation(List<Place> recommendedPlaces, UserPreferences preferences) {
        // 숙소는 소분류 카테고리 B02로 필터링
        return recommendedPlaces.stream()
                .filter(place -> place.getCat1().equals("B02"))
                .findFirst()
                .orElse(null);
    }

}