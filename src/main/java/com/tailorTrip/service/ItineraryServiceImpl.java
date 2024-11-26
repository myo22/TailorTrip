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

    private final MemberService memberService;

    private final ItineraryRepository itineraryRepository;

    private final ModelMapper modelMapper;

    private static final double MIN_DISTANCE = 3.0; // 최소 거리 제약: 10km

    @Override
    public ItineraryDTO createItinerary(UserPreferences preferences) {
        int duration = preferences.getTripDuration();
        List<Place> recommendedPlaces = recommendationService.getRecommendations(preferences);

        // 1. 카테고리별로 장소를 분류하고 숙소를 기준으로 하루 단위 클러스터링 수행
        Map<CategoryType, List<Place>> categorizedPlaces = categorizePlaces(recommendedPlaces);
        List<Place> accommodations = filterAccommodations(categorizedPlaces.getOrDefault(CategoryType.ACCOMMODATION, new ArrayList<>()));
        List<Place> meals = categorizedPlaces.getOrDefault(CategoryType.MEAL, new ArrayList<>());
        List<Place> activities = categorizedPlaces.getOrDefault(CategoryType.ACTIVITY, new ArrayList<>());

        Set<Place> usedMeals = new HashSet<>();
        Set<Place> usedActivities = new HashSet<>();

        List<ItineraryDay> itineraryDays = new ArrayList<>();

        // 2. 속도 설정에 따른 식사와 활동 수 결정
        int maxMeals = 3;       // 기본값: 3 (아침, 점심, 저녁)
        int maxActivities = 3;  // 기본값: 3 (오전, 오후, 저녁)

        switch (preferences.getTravelPace().toLowerCase()) {
            case "느긋하게":
                maxMeals = 2;        // 점심, 저녁
                maxActivities = 3;   // 오전, 오후, 저녁 활동
                break;
            case "바쁘게":
                maxMeals = 3;        // 아침, 점심, 저녁
                maxActivities = 4;   // 오전, 오후, 저녁 활동 + 추가 활동
                break;
            default: // "보통"인 경우
                // 기본값 유지
                break;
        }


        // 3. 숙소를 기준으로 일정 구성 (1일인 경우 숙소 제외)
        for (int day = 1; day <= duration; day++) {
            List<Place> dailyPlaces = new ArrayList<>();

            // 하루 일정만 있을 경우 숙소를 제외하고 활동/식사만 추가
            if (duration == 1) {
                // 숙소 제외하고 식사와 활동만 추가
                List<Place> nearbyMeals = findNearbyPlaces(null, meals, usedMeals);
                List<Place> nearbyActivities = findNearbyPlaces(null, activities, usedActivities);

                // 식사와 활동을 번갈아 가며 추가
                int addedMeals = 0;
                int addedActivities = 0;

                // "느긋하게"인 경우 활동 -> 식사 순으로 추가
                boolean relaxedPace = preferences.getTravelPace().equalsIgnoreCase("느긋하게");

                for (int i = 0; i < Math.max(nearbyMeals.size(), nearbyActivities.size()); i++) {
                    if (relaxedPace) {
                        if (addedActivities < maxActivities && i < nearbyActivities.size()) {
                            dailyPlaces.add(nearbyActivities.get(i)); // 활동 추가
                            usedActivities.add(nearbyActivities.get(i));
                            addedActivities++;
                        }
                        if (addedMeals < maxMeals && i < nearbyMeals.size()) {
                            dailyPlaces.add(nearbyMeals.get(i)); // 식사 추가
                            usedMeals.add(nearbyMeals.get(i));
                            addedMeals++;
                        }
                    } else {
                        if (addedMeals < maxMeals && i < nearbyMeals.size()) {
                            dailyPlaces.add(nearbyMeals.get(i)); // 식사 추가
                            usedMeals.add(nearbyMeals.get(i));
                            addedMeals++;
                        }
                        if (addedActivities < maxActivities && i < nearbyActivities.size()) {
                            dailyPlaces.add(nearbyActivities.get(i)); // 활동 추가
                            usedActivities.add(nearbyActivities.get(i));
                            addedActivities++;
                        }
                    }
                    if (addedMeals >= maxMeals && addedActivities >= maxActivities) break;
                }
            } else {
                // 2박 이상일 경우, 숙소를 추가
                Place currentAccommodation = accommodations.get((day - 1) / 3 % accommodations.size()); // 3일마다 숙소 변경

                // 근처의 활동 및 식사 장소 클러스터링
                List<Place> nearbyMeals = findNearbyPlaces(currentAccommodation, meals, usedMeals);
                List<Place> nearbyActivities = findNearbyPlaces(currentAccommodation, activities, usedActivities);

                // 식사와 활동을 번갈아 가며 추가
                int addedMeals = 0;
                int addedActivities = 0;

                // "느긋하게"인 경우 활동 -> 식사 순으로 추가
                boolean relaxedPace = preferences.getTravelPace().equalsIgnoreCase("느긋하게");

                for (int i = 0; i < Math.max(nearbyMeals.size(), nearbyActivities.size()); i++) {
                    if (relaxedPace) {
                        if (addedActivities < maxActivities && i < nearbyActivities.size()) {
                            dailyPlaces.add(nearbyActivities.get(i)); // 활동 추가
                            usedActivities.add(nearbyActivities.get(i));
                            addedActivities++;
                        }
                        if (addedMeals < maxMeals && i < nearbyMeals.size()) {
                            dailyPlaces.add(nearbyMeals.get(i)); // 식사 추가
                            usedMeals.add(nearbyMeals.get(i));
                            addedMeals++;
                        }
                    } else {
                        if (addedMeals < maxMeals && i < nearbyMeals.size()) {
                            dailyPlaces.add(nearbyMeals.get(i)); // 식사 추가
                            usedMeals.add(nearbyMeals.get(i));
                            addedMeals++;
                        }
                        if (addedActivities < maxActivities && i < nearbyActivities.size()) {
                            dailyPlaces.add(nearbyActivities.get(i)); // 활동 추가
                            usedActivities.add(nearbyActivities.get(i));
                            addedActivities++;
                        }
                    }
                    if (addedMeals >= maxMeals && addedActivities >= maxActivities) break;
                }

                // 숙소는 항상 마지막에 추가
                dailyPlaces.add(currentAccommodation);
            }

            // 4. 해당 하루 일정에 대해 TSP 경로 생성
            List<Place> optimalPath = generateOptimalPath(dailyPlaces);

            // 4-1. 순서를 복원: TSP 결과를 원래 의도한 순서대로 정렬
            List<Place> orderedPath = reorderDailyPlaces(optimalPath, dailyPlaces);

            // 5. 최적 경로에 따라 일정 항목 생성
            List<ItineraryItem> items = createItineraryItems(orderedPath, usedMeals, usedActivities);
            itineraryDays.add(ItineraryDay.builder().dayNumber(day).items(items).build());
        }

        return ItineraryDTO.builder().duration(duration).days(itineraryDays).build();
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

    /**
     * TSP 결과를 원래의 의도된 순서로 복원하는 메서드
     */
    private List<Place> reorderDailyPlaces(List<Place> tspPath, List<Place> originalOrder) {
        List<Place> reorderedPlaces = new ArrayList<>();

        for (Place place : originalOrder) {
            if (tspPath.contains(place)) {
                reorderedPlaces.add(place);
            }
        }

        return reorderedPlaces;
    }

    private List<Place> filterAccommodations(List<Place> accommodations) {
        List<Place> filteredAccommodations = new ArrayList<>();
        Place lastAccommodation = null;

        for (Place accommodation : accommodations) {
            if (lastAccommodation == null || calculateDistance(
                    lastAccommodation.getMapY(), lastAccommodation.getMapX(),
                    accommodation.getMapY(), accommodation.getMapX()) >= MIN_DISTANCE) {
                filteredAccommodations.add(accommodation);
                lastAccommodation = accommodation;
            }
        }
        return filteredAccommodations;
    }

    private List<ItineraryItem> createItineraryItems(List<Place> optimalPath, Set<Place> usedMeals, Set<Place> usedActivities) {
        List<ItineraryItem> items = new ArrayList<>();

        for (Place place : optimalPath) {
            CategoryType categoryType = determineCategoryType(place);
            String timeOfDay;

            // 카테고리에 따라 timeOfDay 값 설정
            switch (categoryType) {
                case MEAL -> timeOfDay = "음식";
                case ACTIVITY -> timeOfDay = "활동";
                case ACCOMMODATION -> timeOfDay = "숙박";
                default -> timeOfDay = "기타";
            }

            items.add(ItineraryItem.builder()
                    .timeOfDay(timeOfDay)
                    .place(place)
                    .activityType(categoryType.toString()) // 여전히 영어로 저장하고 싶다면 유지
                    .build()
            );

            if (categoryType == CategoryType.MEAL) usedMeals.add(place);
            else if (categoryType == CategoryType.ACTIVITY) usedActivities.add(place);
        }
        return items;
    }

    private List<Place> findNearbyPlaces(Place accommodation, List<Place> places, Set<Place> usedPlaces) {
        if (accommodation == null) {
            // 거리 계산 없이 사용하지 않은 장소만 반환
            return places.stream()
                    .filter(place -> !usedPlaces.contains(place))
                    .collect(Collectors.toList());
        }

        // 숙소가 있을 경우 거리 계산
        return places.stream()
                .filter(place -> !usedPlaces.contains(place))
                .sorted(Comparator.comparingDouble(place -> calculateDistance(
                        accommodation.getMapY(), accommodation.getMapX(), place.getMapY(), place.getMapX())))
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