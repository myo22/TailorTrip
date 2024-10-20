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
import org.nd4j.linalg.ops.transforms.Transforms;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class RecommendationServiceImpl implements RecommendationService {

    private final PlaceRepository placeRepository;

    private final RecommendationModel recommendationModel;

    private final DataPreprocessor dataPreprocessor;

    @Override
    public Set<Place> getRecommendations(UserPreferences preferences) {

        // 데이터베이스 쿼리를 통해 지역별로 필터링된 장소들 가져오기
        List<Place> regionalPlaces = placeRepository.findByAddr1Containing(preferences.getRegion());


        // 1. 사용자 선호도를 벡터로 전처리
        INDArray input = dataPreprocessor.preprocessUserPreferences(preferences);

        // 모델을 사용하여 예측 (멀티레이블)
        INDArray output = recommendationModel.predict(input);


        // 장소별 점수 계산
        Map<String, List<PlaceScore>> categorizedPlaces = new HashMap<>();
        List<Place> petFriendlyPlaces = new ArrayList<>(); // 애완동물 동반 가능한 장소 리스트
        for (Place place : regionalPlaces) {
            INDArray placeLabel = dataPreprocessor.preprocessPlaceCategories(place.getCat1(), place.getCat2(), place.getCat3());
            double score = cosineSimilarity(output, placeLabel);

            // 애완동물 동반 여부 체크
            if (preferences.isPetFriendly() && place.getAcmpyTypeCd().contains("동반가능")) {
                petFriendlyPlaces.add(place); // 애완동물 동반 가능한 장소 리스트에 추가
            }

            // 카테고리별로 장소 추가
            categorizedPlaces.computeIfAbsent(place.getCat1(), k -> new ArrayList<>())
                    .add(new PlaceScore(place, score));
        }

        Set<Place> topPlaces = new HashSet<>(); // 중복 제거를 위해 Set 사용

        // 각 카테고리에서 상위 100개 장소 선택
        for (Map.Entry<String, List<PlaceScore>> entry : categorizedPlaces.entrySet()) {
            List<PlaceScore> scoredPlaces = entry.getValue();

            // 애완동물 동반 장소가 있는 경우, 먼저 해당 장소 추가
            if (preferences.isPetFriendly()) {
                topPlaces.addAll(petFriendlyPlaces.stream()
                        .filter(p -> p.getCat1().equals(entry.getKey()))
                        .collect(Collectors.toList()));
            }

            // 점수로 정렬하여 상위 100개 선택
            List<PlaceScore> topScoredPlaces = scoredPlaces.stream()
                    .sorted(Comparator.comparingDouble(PlaceScore::getScore).reversed())
                    .limit(100 - topPlaces.size()) // 애완동물 동반 장소가 포함된 경우 나머지 장소로 100개 맞추기
                    .collect(Collectors.toList());


            // 최종 장소 리스트에 추가
            topPlaces.addAll(topScoredPlaces.stream().map(PlaceScore::getPlace).collect(Collectors.toList()));
        }

        return topPlaces; // 최종 장소 리스트 반환
    }

    private double cosineSimilarity(INDArray vectorA, INDArray vectorB) {
        // 두 벡터가 2D인지 확인하고, 아니라면 reshape
        if (vectorA.rank() < 2) {
            vectorA = vectorA.reshape(1, vectorA.length());
        }
        if (vectorB.rank() < 2) {
            vectorB = vectorB.reshape(1, vectorB.length());
        }

        // 코사인 유사도 계산 (Transforms.cosineSim 사용)
        return Transforms.cosineSim(vectorA, vectorB);
    }


//    private List<Place> filterByGeographicalProximity(List<Place> places, UserPreferences preferences) {
//        // 사용자 위치를 기반으로 지리적 근접성 필터링 (예: 중앙 좌표 또는 사용자 입력 좌표)
//        // 여기서는 임의의 중심 좌표를 가정 (예: 서울 시내)
//        double userLat = preferences.getUserLat();
//        double userLng = preferences.getUserLng();
//
//        return places.stream()
//                .sorted(Comparator.comparingDouble(place -> distance(userLat, userLng, place.getMapy(), place.getMapx())))
//                .limit(10) // 가까운 상위 10개 장소 선택
//                .collect(Collectors.toList());
//    }
//
//    // 두 지점 간의 거리 계산 (Haversine 공식 사용)
//    private double distance(double lat1, double lon1, double lat2, double lon2) {
//        final int R = 6371; // 지구 반지름 (km)
//        double latDistance = Math.toRadians(lat2 - lat1);
//        double lonDistance = Math.toRadians(lon2 - lon1);
//        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
//                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        return R * c; // 거리 (km)
//    }

    @Getter
    @AllArgsConstructor
    private static class PlaceScore {
        private Place place;
        private double score;
    }

}
