package com.tailorTrip.service;

import com.tailorTrip.Repository.ItineraryItemRepository;
import com.tailorTrip.Repository.ItineraryRepository;
import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.Repository.UserPreferencesRepository;
import com.tailorTrip.domain.*;
import com.tailorTrip.ml.DataPreprocessor;
import com.tailorTrip.ml.RecommendationModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class RecommendationService {

    private final PlaceRepository placeRepository;

    private final UserPreferencesRepository userPreferencesRepository;

    private final ItineraryRepository itineraryRepository;

    private final ItineraryItemRepository itineraryItemRepository;

    private final RecommendationModel recommendationModel;

    private final DataPreprocessor dataPreprocessor;

    private final DirectionsService directionsService;

    @Transactional
    public List<Itinerary> getRecommendations(UserPreferences preferences) {
        // 1. 사용자 선호도 전처리
        INDArray input = dataPreprocessor.preprocessUserPreferences(preferences);

        // 2. 딥러닝 모델 예측
        int predictedClass = recommendationModel.predict(input);

        // 3. 예측된 클래스를 기반으로 장소 필터링
        List<Place> recommendedPlaces = fetchRecommendedPlaces(predictedClass, preferences);

        // 4. 여행 일정 생성
        List<Itinerary> itineraries = createItineraries(recommendedPlaces, preferences);

        // 5. 데이터베이스에 저장
        itineraryRepository.saveAll(itineraries);

        return itineraries;
    }

    private List<Place> fetchRecommendedPlaces(int predictedClass, UserPreferences preferences) {
        // 예시: predictedClass에 따라 카테고리를 필터링
        String category;
        switch (predictedClass) {
            case 0:
                category = "cafe";
                break;
            case 1:
                category = "restaurant";
                break;
            case 2:
                category = "tourist attraction";
                break;
            default:
                category = "cafe";
        }

        // 사용자 관심사와 일치하는 장소 필터링
        List<Place> places = placeRepository.findAll().stream()
                .filter(place -> place.getCategory().equalsIgnoreCase(category))
                .sorted(Comparator.comparingDouble(Place::getRating).reversed())
                .limit(10) // 상위 10개 장소 선택
                .collect(Collectors.toList());

        return places;
    }

    private List<Itinerary> createItineraries(List<Place> places, UserPreferences preferences) {
        int duration = parseDuration(preferences.getDuration());

        List<Itinerary> itineraries = new ArrayList<>();

        // 예시: 3개의 추천 일정 생성
        for (int i = 0; i < 3; i++) {
            Itinerary itinerary = Itinerary.builder()
                    .duration(duration)
                    .createdAt(LocalDateTime.now())
                    .build();

            List<ItineraryItem> items = new ArrayList<>();

            // 아침식사
            Place breakfastPlace = selectPlace(places, "cafe", preferences);
            if (breakfastPlace != null) {
                ItineraryItem breakfast = ItineraryItem.builder()
                        .itinerary(itinerary)
                        .startTime(LocalTime.of(8, 0))
                        .endTime(LocalTime.of(9, 0))
                        .place(breakfastPlace)
                        .activityType("아침식사")
                        .build();
                items.add(breakfast);
            }

            // 오전 활동
            Place morningActivity = selectPlace(places, "tourist attraction", preferences);
            if (morningActivity != null) {
                ItineraryItem morning = ItineraryItem.builder()
                        .itinerary(itinerary)
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(12, 0))
                        .place(morningActivity)
                        .activityType("오전 활동")
                        .build();
                items.add(morning);
            }

            // 점심식사
            Place lunchPlace = selectPlace(places, "restaurant", preferences);
            if (lunchPlace != null) {
                ItineraryItem lunch = ItineraryItem.builder()
                        .itinerary(itinerary)
                        .startTime(LocalTime.of(12, 30))
                        .endTime(LocalTime.of(13, 30))
                        .place(lunchPlace)
                        .activityType("점심식사")
                        .build();
                items.add(lunch);
            }

            // 오후 활동
            Place afternoonActivity = selectPlace(places, "tourist attraction", preferences);
            if (afternoonActivity != null) {
                ItineraryItem afternoon = ItineraryItem.builder()
                        .itinerary(itinerary)
                        .startTime(LocalTime.of(14, 0))
                        .endTime(LocalTime.of(16, 0))
                        .place(afternoonActivity)
                        .activityType("오후 활동")
                        .build();
                items.add(afternoon);
            }

            // 저녁식사
            Place dinnerPlace = selectPlace(places, "restaurant", preferences);
            if (dinnerPlace != null) {
                ItineraryItem dinner = ItineraryItem.builder()
                        .itinerary(itinerary)
                        .startTime(LocalTime.of(18, 0))
                        .endTime(LocalTime.of(19, 30))
                        .place(dinnerPlace)
                        .activityType("저녁식사")
                        .build();
                items.add(dinner);
            }

            // 숙소 (여행 기간이 1박 이상일 경우)
            if (duration > 1) {
                Place accommodation = selectPlace(places, "hotel", preferences);
                if (accommodation != null) {
                    ItineraryItem stay = ItineraryItem.builder()
                            .itinerary(itinerary)
                            .startTime(LocalTime.of(20, 0))
                            .endTime(LocalTime.of(22, 0))
                            .place(accommodation)
                            .activityType("숙소")
                            .build();
                    items.add(stay);
                }
            }

            // 이동 시간 고려하여 활동 순서 최적화 (필요 시)
            // DirectionsService를 이용하여 장소 간 최적 경로 계산
            // 여기에 추가 로직을 구현할 수 있습니다.

            itinerary.setItems(items);
            itineraries.add(itinerary);
        }

        return itineraries;
    }

    private int parseDuration(String durationStr) {
        switch (durationStr) {
            case "하루":
                return 1;
            case "주말":
                return 2;
            case "일주일":
                return 7;
            default:
                return 1;
        }
    }

    private Place selectPlace(List<Place> places, String category, UserPreferences preferences) {
        // 카테고리에 맞고, 별점 높은 순으로 정렬하여 첫 번째 장소 선택
        return places.stream()
                .filter(place -> place.getCategory().equalsIgnoreCase(category))
                .sorted(Comparator.comparingDouble(Place::getRating).reversed()
                        .thenComparingInt(Place::getUserRatingsTotal).reversed()) // 별점과 평점 수 기준 정렬
                .findFirst()
                .orElse(null);
    }
}
