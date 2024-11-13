package com.tailorTrip.service;

import com.tailorTrip.Repository.ItineraryRepository;
import com.tailorTrip.domain.*;
import com.tailorTrip.dto.ItineraryDTO;
import com.tailorTrip.dto.ItineraryDay;
import com.tailorTrip.dto.ItineraryItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ItineraryServiceImpl implements ItineraryService {

    private final RecommendationService recommendationService;

    private final KorService korService;

    private final MemberService memberService;

    private final ItineraryRepository itineraryRepository;

    private final ModelMapper modelMapper;

    private final PlaceService placeService;

    @Override
    public ItineraryDTO createItinerary(UserPreferences preferences) {
        int duration = preferences.getTripDuration(); // 여행 기간
        List<Place> recommendedPlaces = recommendationService.getRecommendations(preferences); // 100개의 장소들

        // 1. 카테고리별로 장소를 분류하고 숙소를 기준으로 하루 단위 클러스터링 수행
        Map<Integer, List<Place>> dailyClusters = categorizeAndClusterPlaces(recommendedPlaces, preferences, duration);

        List<ItineraryDay> itineraryDays = new ArrayList<>();

        // 2. 각 하루 단위 클러스터에 대해 최적의 경로 생성
        for (int day = 1; day <= duration; day++) {
            List<Place> dailyPlaces = dailyClusters.get(day);
            List<Place> optimalPath = generateOptimalPath(dailyPlaces);

            List<ItineraryItem> items = new ArrayList<>();
            int mealCount = 0, activityCount = 0;

            // 최적 경로에 따라 일정 항목 생성
            for (Place place : optimalPath) {
                if (dailyPlaces.contains(place)) {
                    String activityType = determineCategoryType(place).toString();
                    String timeOfDay = (activityType.equals("MEAL")) ? determineMealTimeOfDay(mealCount++) : determineActivityTimeOfDay(activityCount++);

                    items.add(ItineraryItem.builder()
                            .timeOfDay(timeOfDay)
                            .place(place)
                            .activityType(activityType)
                            .build()
                    );
                }
            }

            itineraryDays.add(ItineraryDay.builder().dayNumber(day).items(items).build());
        }


        // Place의 overview를 업데이트
        for (ItineraryDay itineraryDay : itineraryDays) {
            for (ItineraryItem item : itineraryDay.getItems()) {
                Place place = item.getPlace();
                updatePlaceInformation(place);
            }
        }

        return ItineraryDTO.builder()
                .duration(duration)
                .days(itineraryDays)
                .build();
    }

    @Override
    public void saveItinerary(ItineraryDTO itineraryDTO, String userId) {
        Member member = memberService.findMemberById(userId);

        Itinerary itinerary = modelMapper.map(itineraryDTO, Itinerary.class);
        itinerary.assignMember(member); // Using custom method
        itineraryRepository.save(itinerary);
    }

    public void updateItinerary(String userId, Long id, ItineraryDTO itineraryDTO) {
        // 기존 일정을 데이터베이스에서 가져오기
        Optional<Itinerary> result = itineraryRepository.findByIdAndMemberMid(id, userId);

        Itinerary existingItinerary = result.orElseThrow();

        // DTO의 값으로 기존 일정을 업데이트
        modelMapper.map(itineraryDTO, existingItinerary);

        // 업데이트된 일정 저장
        itineraryRepository.save(existingItinerary);
    }

    @Override
    public ItineraryDTO getItineraryById(Long id){
        Optional<Itinerary> result = itineraryRepository.findById(id);
        Itinerary itinerary = result.orElseThrow();
        return modelMapper.map(itinerary, ItineraryDTO.class);
    }

    private Map<Integer, List<Place>> categorizeAndClusterPlaces(List<Place> recommendedPlaces, UserPreferences preferences, int duration) {
        Map<CategoryType, List<Place>> categorizedPlaces = categorizePlaces(recommendedPlaces);
        List<Place> accommodations = categorizedPlaces.getOrDefault(CategoryType.ACCOMMODATION, new ArrayList<>());

        Map<Integer, List<Place>> dailyClusters = new HashMap<>();

        for (int day = 1; day <= duration; day++) {
            List<Place> dayCluster = new ArrayList<>();

            Place currentAccommodation = accommodations.get(day % accommodations.size());

            // 클러스터링: 숙소와 가까운 활동 및 식사 장소 선택
            List<Place> nearbyMeals = findNearbyPlaces(currentAccommodation, categorizedPlaces.get(CategoryType.MEAL));
            List<Place> nearbyActivities = findNearbyPlaces(currentAccommodation, categorizedPlaces.get(CategoryType.ACTIVITY));

            // 각 하루 일정에 대해 일정 수의 식사와 활동 장소를 추가
            dayCluster.add(currentAccommodation);
            dayCluster.addAll(nearbyMeals.subList(0, Math.min(nearbyMeals.size(), 3)));
            dayCluster.addAll(nearbyActivities.subList(0, Math.min(nearbyActivities.size(), 3)));

            dailyClusters.put(day, dayCluster);
        }
        return dailyClusters;
    }

    private List<Place> findNearbyPlaces(Place accommodation, List<Place> places) {
        return places.stream()
                .sorted(Comparator.comparingDouble(place -> calculateDistance(accommodation.getMapY(), accommodation.getMapX(), place.getMapY(), place.getMapX())))
                .collect(Collectors.toList());
    }

    private List<Place> generateOptimalPath(List<Place> places) {
        List<Place> optimalPath = new ArrayList<>();
        if (places.isEmpty()) return optimalPath;

        Place startPlace = places.get(0); // 시작점을 숙소로 설정
        optimalPath.add(startPlace);

        Set<Place> visited = new HashSet<>();
        visited.add(startPlace);

        while (visited.size() < places.size()) {
            Place lastPlace = optimalPath.get(optimalPath.size() - 1);
            Place nearestPlace = null;
            double nearestDistance = Double.MAX_VALUE;

            for (Place place : places) {
                if (visited.contains(place)) continue;
                double distance = calculateDistance(lastPlace.getMapY(), lastPlace.getMapX(), place.getMapY(), place.getMapX());
                if (distance < nearestDistance) {
                    nearestPlace = place;
                    nearestDistance = distance;
                }
            }

            if (nearestPlace != null) {
                optimalPath.add(nearestPlace);
                visited.add(nearestPlace);
            }
        }

        return optimalPath;
    }


    private void updatePlaceInformation(Place place) {
        // overview가 이미 존재하는지 확인
        if (place.getOverview() == null || place.getOverview().isEmpty()) {
            try {
                String overview = korService.getOverview(place.getContentId(), place.getContentTypeId());
                System.out.println(overview);

                // PlaceService를 통해 Place 업데이트
                placeService.updatePlaceOverview(place, overview);

                // 요청 간의 지연 (예: 1초)
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("요청 간의 지연 중 오류 발생: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error fetching overview for place " + place.getContentId() + ": " + e.getMessage());
            }
        }

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
        return switch (place.getCat1()) {
            case "A05" -> CategoryType.MEAL;
            case "B02" -> CategoryType.ACCOMMODATION;
            default -> CategoryType.ACTIVITY; // 기본적으로 활동으로 분류
        };
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
        return switch (index) {
            case 0 -> "아침";
            case 1 -> "점심";
            case 2 -> "저녁";
            default -> "식사";
        };
    }

    private String determineActivityTimeOfDay(int index) {
        return switch (index) {
            case 0 -> "오전 활동";
            case 1, 2 -> "오후 활동";
            case 3 -> "저녁 활동";
            default -> "활동";
        };
    }

    // 숙소의 위치도 포함하여 MST 경로를 생성하는 메서드
    private List<Place> generateMSTPath(List<Place> places) {
        List<Place> mstPath = new ArrayList<>();
        Set<Place> visited = new HashSet<>();
        PriorityQueue<Edge> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(edge -> edge.weight));

        // 시작점: 첫 번째 장소
        Place start = places.get(0);
        visited.add(start);
        mstPath.add(start);

        // 인접한 장소들 추가 (모든 장소가 연결될 수 있도록)
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