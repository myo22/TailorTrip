package com.tailorTrip.service;

import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.*;
import com.tailorTrip.ml.DataPreprocessor;
import com.tailorTrip.ml.RecommendationModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
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

        // 데이터베이스 쿼리를 통해 지역별로 필터링된 장소들 가져오기
        List<Place> regionalPlaces = placeRepository.findByAddr1Containing(preferences.getRegion());


        // 1. 사용자 선호도를 벡터로 전처리
        INDArray input = dataPreprocessor.preprocessUserPreferences(preferences);

        // 모델을 사용하여 예측 (멀티레이블)
        INDArray output = recommendationModel.predict(input);


        // 각 장소에 대해 점수 계산
        List<PlaceScore> placeScores = regionalPlaces.stream()
                .map(place -> {
                    INDArray placeLabel = dataPreprocessor.preprocessPlaceCategories(place.getCat1(), place.getCat2(), place.getCat3());
                    double score = cosineSimilarity(output, placeLabel);
                    return new PlaceScore(place, score);
                })
                .collect(Collectors.toList());

        // 점수 순으로 정렬하여 상위 N개 선택
        List<Place> topPlaces = placeScores.stream()
                .sorted(Comparator.comparingDouble(PlaceScore::getScore).reversed())
                .limit(100) // 상위 100개 장소 선택
                .map(PlaceScore::getPlace)
                .collect(Collectors.toList());

        // 지리적 근접성 고려하여 필터링
        List<Place> filteredPlaces = filterByGeographicalProximity(topPlaces, preferences);

        return filteredPlaces;

    }

    private boolean isWithinRegion(String addr1, String selectedRegion) {
        return addr1.contains(selectedRegion);
    }

    private double cosineSimilarity(INDArray vectorA, INDArray vectorB) {
        INDArray dotProductMatrix = vectorA.mmul(vectorB.transpose());
        double dotProduct = dotProductMatrix.getDouble(0);
        double normA = vectorA.norm2Number().doubleValue();
        double normB = vectorB.norm2Number().doubleValue();
        return dotProduct / (normA * normB);
    }

    private List<Place> filterByGeographicalProximity(List<Place> places, UserPreferences preferences) {
        // 사용자 위치를 기반으로 지리적 근접성 필터링 (예: 중앙 좌표 또는 사용자 입력 좌표)
        // 여기서는 임의의 중심 좌표를 가정 (예: 서울 시내)
        double userLat = preferences.getUserLat();
        double userLng = preferences.getUserLng();

        return places.stream()
                .sorted(Comparator.comparingDouble(place -> distance(userLat, userLng, place.getMapy(), place.getMapx())))
                .limit(10) // 가까운 상위 10개 장소 선택
                .collect(Collectors.toList());
    }

    // 두 지점 간의 거리 계산 (Haversine 공식 사용)
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반지름 (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // 거리 (km)
    }

    @Getter
    @AllArgsConstructor
    private static class PlaceScore {
        private Place place;
        private double score;
    }

}
