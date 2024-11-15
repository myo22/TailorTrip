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

    private static final double MIN_DISTANCE = 10.0; // 최소 거리 제약: 10km

    @Override
    public ItineraryDTO createItinerary(UserPreferences preferences) {
        int duration = preferences.getTripDuration();
        List<Place> recommendedPlaces = recommendationService.getRecommendations(preferences);

        // 1. 카테고리별로 장소를 분류하고 숙소를 기준으로 하루 단위 클러스터링 수행
        Map<CategoryType, List<Place>> categorizedPlaces = categorizePlaces(recommendedPlaces);
        List<Place> accommodations = categorizedPlaces.getOrDefault(CategoryType.ACCOMMODATION, new ArrayList<>());
        List<Place> meals = categorizedPlaces.getOrDefault(CategoryType.MEAL, new ArrayList<>());
        List<Place> activities = categorizedPlaces.getOrDefault(CategoryType.ACTIVITY, new ArrayList<>());

        Set<Place> usedMeals = new HashSet<>();
        Set<Place> usedActivities = new HashSet<>();

        List<ItineraryDay> itineraryDays = new ArrayList<>();

        // 2. 숙소를 기준으로 일정 구성
        for (int day = 1; day <= duration; day++) {
            Place currentAccommodation = accommodations.get((day - 1) / 3 % accommodations.size()); // 3일마다 숙소 변경
            List<Place> dayMeals = new ArrayList<>();
            List<Place> dayActivities = new ArrayList<>();

            // 3. 숙소를 기준으로 근처의 활동 및 식사 장소 클러스터링
            List<Place> nearbyMeals = findNearbyPlaces(currentAccommodation, meals, usedMeals);
            List<Place> nearbyActivities = findNearbyPlaces(currentAccommodation, activities, usedActivities);

            // 3일마다 숙소 변경하므로 하루 일정에 맞는 식사 및 활동 추가
            List<Place> dailyPlaces = new ArrayList<>();
            dailyPlaces.add(currentAccommodation);

            // 식사와 활동을 최대 3개씩 추가
            dayMeals.addAll(nearbyMeals.subList(0, Math.min(3, nearbyMeals.size())));
            dayActivities.addAll(nearbyActivities.subList(0, Math.min(3, nearbyActivities.size())));

            dailyPlaces.addAll(dayMeals);
            dailyPlaces.addAll(dayActivities);

            // 4. 해당 하루 일정에 대해 TSP 경로 생성
            List<Place> optimalPath = generateOptimalPath(dailyPlaces);

            List<ItineraryItem> items = new ArrayList<>();
            int mealCount = 0, activityCount = 0;

            // 5. 최적 경로에 따라 일정 항목 생성
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

                    // 사용된 장소는 중복을 피하기 위해 기록
                    if (activityType.equals("MEAL")) {
                        usedMeals.add(place);
                    } else {
                        usedActivities.add(place);
                    }
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

    private List<Place> findNearbyPlaces(Place accommodation, List<Place> places, Set<Place> usedPlaces) {
        return places.stream()
                .filter(place -> !usedPlaces.contains(place)) // 이미 사용된 장소는 제외
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

                // 최소 거리 제약을 추가
                if (distance >= MIN_DISTANCE && distance < nearestDistance) {
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