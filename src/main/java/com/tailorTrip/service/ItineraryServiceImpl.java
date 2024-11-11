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

        // 장소를 카테고리별로 분류
        Map<CategoryType, List<Place>> categorizedPlaces = categorizePlaces(recommendedPlaces);

        // 숙소, 식사, 활동 장소
        List<Place> accommodations = categorizedPlaces.getOrDefault(CategoryType.ACCOMMODATION, new ArrayList<>());
        List<Place> meals = categorizedPlaces.getOrDefault(CategoryType.MEAL, new ArrayList<>());
        List<Place> activities = categorizedPlaces.getOrDefault(CategoryType.ACTIVITY, new ArrayList<>());

        // 여행 일정 동안 사용될 숙소 리스트를 가져옴
        List<Place> selectedAccommodations = selectAccommodationForDays(accommodations, preferences, duration);

        // MST에 포함할 모든 장소들을 리스트로 합침 (숙소도 포함)
        List<Place> allPlaces = new ArrayList<>(meals);
        allPlaces.addAll(activities);
        allPlaces.addAll(selectedAccommodations);

        // TSP 경로 생성
        List<Place> optimalPath = generateOptimalPath(allPlaces);

        List<ItineraryDay> itineraryDays = createItineraryDays(optimalPath, meals, activities, selectedAccommodations, preferences, duration);


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

    private List<Place> generateOptimalPath(List<Place> places) {
        List<Place> optimalPath = new ArrayList<>();
        if (places.isEmpty()) return optimalPath;

        Place startPlace = places.get(0);
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

    private List<ItineraryDay> createItineraryDays(List<Place> optimalPath, List<Place> meals, List<Place> activities,
                                                   List<Place> accommodations, UserPreferences preferences, int duration) {
        List<ItineraryDay> itineraryDays = new ArrayList<>();
        int mealsPerDay = preferences.getTravelPace().equalsIgnoreCase("느긋하게") ? 2 : 3;
        int activitiesPerDay = preferences.getTravelPace().equalsIgnoreCase("바쁘게") ? 4 : 3;

        Set<Place> usedPlaces = new HashSet<>();

        for (int day = 1; day <= duration; day++) {
            List<ItineraryItem> items = new ArrayList<>();
            addMealAndActivityItems(optimalPath, meals, activities, mealsPerDay, activitiesPerDay, usedPlaces, items);

            if (!accommodations.isEmpty()) {
                int accommodationIndex = (day - 1) / 3;
                if (accommodationIndex < accommodations.size()) {
                    Place accommodation = accommodations.get(accommodationIndex);
                    items.add(ItineraryItem.builder().timeOfDay("숙소").place(accommodation).activityType("숙박").build());
                }
            }

            itineraryDays.add(ItineraryDay.builder().dayNumber(day).items(items).build());
        }

        return itineraryDays;
    }

    private void addMealAndActivityItems(List<Place> path, List<Place> meals, List<Place> activities, int mealsPerDay,
                                         int activitiesPerDay, Set<Place> usedPlaces, List<ItineraryItem> items) {
        int mealCount = 0;
        int activityCount = 0;

        for (Place place : path) {
            if (usedPlaces.contains(place)) continue;

            if (meals.contains(place) && mealCount < mealsPerDay) {
                items.add(ItineraryItem.builder().timeOfDay(determineMealTimeOfDay(mealCount)).place(place).activityType("식사").build());
                usedPlaces.add(place);
                mealCount++;
            } else if (activities.contains(place) && activityCount < activitiesPerDay) {
                items.add(ItineraryItem.builder().timeOfDay(determineActivityTimeOfDay(activityCount)).place(place).activityType("활동").build());
                usedPlaces.add(place);
                activityCount++;
            }

            if (mealCount >= mealsPerDay && activityCount >= activitiesPerDay) break;
        }
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
     * 선호 숙소 유형에 맞는 숙소를 선택합니다.
     *
     * @param accommodations 숙소 목록
     * @param preferences     사용자 선호도
     * @param totalDays        여행 기간
     * @return 선택된 숙소
     */
    private List<Place> selectAccommodationForDays(List<Place> accommodations, UserPreferences preferences, int totalDays) {
        if (accommodations.isEmpty()) return Collections.emptyList();

        // 숙소 교체 주기 설정 (예: 3일마다 교체)
        int accommodationChangeInterval = 3;

        // 각 숙소에 대한 선호도에 따라 필터링
        List<Place> preferredAccommodations = accommodations.stream()
                .filter(place -> preferences.getAccommodationPreference().stream()
                        .anyMatch(preference -> preference.equalsIgnoreCase(place.getCat3())))
                .collect(Collectors.toList());

        // 선호 숙소가 있으면 사용하고, 없으면 모든 숙소에서 선택
        List<Place> accommodationsToUse = preferredAccommodations.isEmpty() ? accommodations : preferredAccommodations;

        List<Place> selectedAccommodations = new ArrayList<>();
        for (int day = 0; day < totalDays; day += accommodationChangeInterval) {
            int index = (day / accommodationChangeInterval) % accommodationsToUse.size();
            selectedAccommodations.add(accommodationsToUse.get(index));
        }
        return selectedAccommodations;
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

    private String determineActivityType(Place place) {
        // 장소의 카테고리에 따라 활동 유형 결정
        return switch (place.getCat1()) {
            case "A01" -> "자연 관광";
            case "A02" -> "문화/역사 탐방";
            case "A03" -> "레포츠";
            default -> "관광";
        };
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