package com.tailorTrip.service;

import com.tailorTrip.Repository.PlaceRepository;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.tailorTrip.ml.DataPreprocessor.CATEGORY_MAP;

@Service
@RequiredArgsConstructor
@Log4j2
public class RecommendationService {

    private final PlaceRepository placeRepository;

    private final RecommendationModel recommendationModel;

    private final DataPreprocessor dataPreprocessor;


    public List<Place> getRecommendations(UserPreferences preferences) {
        // 1. 사용자 선호도를 벡터로 전처리
        INDArray input = dataPreprocessor.preprocessUserPreferences(preferences);

        // 모델을 사용하여 예측 (멀티레이블)
        INDArray output = recommendationModel.predict(input);

        // 예측된 확률이 일정 임계값을 넘는 카테고리 선택
        double threshold = 0.5;
        Set<Integer> selectedCategories = new HashSet<>();
        for (int i = 0; i < output.length(); i++) {
            if (output.getDouble(i) > threshold) {
                selectedCategories.add(i);
            }
        }


        // 선택된 카테고리에 해당하는 장소 필터링
        List<Place> allPlaces = placeRepository.findAll();
        List<Place> recommendedPlaces = allPlaces.stream()
                .filter(place -> {
                    int cat1 = CATEGORY_MAP.getOrDefault(place.getCat1(), -1);
                    int cat2 = CATEGORY_MAP.getOrDefault(place.getCat2(), -1);
                    int cat3 = CATEGORY_MAP.getOrDefault(place.getCat3(), -1);
                    return selectedCategories.contains(cat1) ||
                            selectedCategories.contains(cat2) ||
                            selectedCategories.contains(cat3);
                })
                .collect(Collectors.toList());

        // 추천 장소 중 별점 높은 순으로 정렬
        recommendedPlaces.sort(Comparator.comparingDouble(Place::getRating).reversed()
                .thenComparingInt(Place::getUserRatingsTotal).reversed());

        return recommendedPlaces;

    }

}
