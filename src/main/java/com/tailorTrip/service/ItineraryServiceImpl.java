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

    private final KorService korService;

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
        double currentLat = selectedAccommodation != null ? selectedAccommodation.getMapY() : 0.0;
        double currentLng = selectedAccommodation != null ? selectedAccommodation.getMapX() : 0.0;

        // MST 생성
        List<Place> allPlaces = new ArrayList<>(meals);
        allPlaces.addAll(activities);
        allPlaces.add(selectedAccommodation);
        List<Place> mstPath = generateMSTPath(allPlaces);

        List<ItineraryDay> itineraryDays = new ArrayList<>();
        int totalDays = duration;
        Set<Place> usedPlaces = new HashSet<>(); // 이미 사용된 장소를 추적하는 집합

        for (int day = 1; day <= totalDays; day++) {
            List<ItineraryItem> items = new ArrayList<>();

            // 하루에 필요한 장소 수 설정
            int mealsPerDay = 3; // 아침, 점심, 저녁
            int activitiesPerDay = 3; // 오전 1개, 오후 활동 2개

            if (preferences.getTravelPace().equalsIgnoreCase("느긋하게")) {
                mealsPerDay -= 1; // 점심, 저녁
            } else if (preferences.getTravelPace().equalsIgnoreCase("바쁘게")) {
                activitiesPerDay += 1; // 바쁘게: 오전, 오후 2개, 저녁 활동
            }

            // MST 경로에서 장소 선택
            for (Place place : mstPath) {
                if (usedPlaces.contains(place)) continue; // 이미 사용된 장소는 건너뜀
                if (items.size() >= (mealsPerDay + activitiesPerDay)) break;

                // 식사 또는 활동으로 분류하여 일정 아이템 추가
                if (meals.contains(place) && items.stream().filter(item -> item.getActivityType().equals("식사")).count() < mealsPerDay) {
                    items.add(ItineraryItem.builder()
                            .timeOfDay("식사")
                            .place(place)
                            .activityType("식사")
                            .build());
                    usedPlaces.add(place); // 추가된 장소를 usedPlaces에 추가
                } else if (activities.contains(place) && items.stream().filter(item -> item.getActivityType().equals("활동")).count() < activitiesPerDay) {
                    items.add(ItineraryItem.builder()
                            .timeOfDay("활동")
                            .place(place)
                            .activityType("활동")
                            .build());
                    usedPlaces.add(place); // 추가된 장소를 usedPlaces에 추가
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

        for (ItineraryDay itineraryDay : itineraryDays) {
            for (ItineraryItem item : itineraryDay.getItems()) {
                Place place = item.getPlace();

                // overview, intro, detailInfo 업데이트
//                updatePlaceInformation(place);
            }
        }

        return Itinerary.builder()
                .duration(duration)
                .days(itineraryDays)
                .build();
    }

    @Override
    public void saveItinerary(Itinerary itinerary, Member member){
        
    }

//    private void updatePlaceInformation(Place place) {
//        // overview가 이미 존재하는지 확인
//        if (place.getOverview() == null || place.getOverview().isEmpty()) {
//            String overview = korService.getOverview(place.getContentId(), place.getContentTypeId());
//            if (overview != null && !overview.isEmpty()) {
//                place.updateOverview(overview);
//            }
//        }
//
//        // intro 업데이트
//        Map<String, Object> intro = korService.getIntro(place.getContentId(), place.getContentTypeId());
//        if (intro != null) {
//            place.setIntro(intro); // Assuming you have an appropriate setter for intro
//        }
//
//        // detailInfo 업데이트
//        List<DetailInfo> detailInfos = korService.getDetailInfo(place.getContentId(), place.getContentTypeId());
//        if (detailInfos != null) {
//            place.setDetailInfo(detailInfos); // Assuming you have an appropriate setter for detailInfo
//        }
//    }

    private List<Place> generateMSTPath(List<Place> places) {
        // Prim 알고리즘을 사용하여 MST 경로 생성
        List<Place> mstPath = new ArrayList<>();
        Set<Place> visited = new HashSet<>();
        PriorityQueue<Edge> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(edge -> edge.weight));

        // 시작점: 첫 번째 장소
        Place start = places.get(0);
        visited.add(start);
        mstPath.add(start);

        // 인접한 장소들 추가
        for (Place place : places) {
            if (place != start) {
                double distance = calculateDistance(start.getMapY(), start.getMapX(), place.getMapY(), place.getMapX());
                priorityQueue.offer(new Edge(start, place, distance));
            }
        }

        while (!priorityQueue.isEmpty() && visited.size() < places.size()) {
            Edge edge = priorityQueue.poll();
            Place nextPlace = edge.to;

            // 방문하지 않은 장소인 경우
            if (!visited.contains(nextPlace)) {
                visited.add(nextPlace);
                mstPath.add(nextPlace);

                // 새로운 장소에 대한 인접한 장소들 추가
                for (Place place : places) {
                    if (!visited.contains(place)) {
                        double distance = calculateDistance(nextPlace.getMapY(), nextPlace.getMapX(), place.getMapY(), place.getMapX());
                        priorityQueue.offer(new Edge(nextPlace, place, distance));
                    }
                }
            }
        }

        return mstPath;
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
                .collect(Collectors.toList());

        if (!preferredAccommodations.isEmpty()) {
            return preferredAccommodations.get(0); // 평점이 가장 높은 숙소 선택
        } else {
            // 선호 숙소 유형이 없을 경우 평점이 높은 숙소 선택
            return accommodations.stream()
                    .findFirst()
                    .orElse(accommodations.get(0));
        }
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
            case 1, 2:
                return "오후 활동";
            case 3:
                return "저녁 활동";
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

    private static class Edge {
        Place from;
        Place to;
        double weight;

        Edge(Place from, Place to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    private enum CategoryType {
        MEAL, ACTIVITY, ACCOMMODATION
    }
}