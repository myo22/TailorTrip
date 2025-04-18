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

    private final PlaceService placeService;

    private final RecommendationModel recommendationModel;

    private final DataPreprocessor dataPreprocessor;


    @Override
    public List<Place> getRecommendations(UserPreferences preferences) {

        // 데이터베이스 쿼리를 통해 지역별로 필터링된 장소들 가져오기
        List<Place> regionalPlaces = placeService.getRegionalPlaces(preferences.getRegion());

        // 1. 사용자 선호도를 벡터로 전처리
        INDArray input = dataPreprocessor.preprocessUserPreferences(preferences);

        // 전처리된 선호도 데이터 로그 출력
        log.debug("Processed User Preferences: {}", input);

        // 모델을 사용하여 예측 (멀티레이블)
        INDArray output = recommendationModel.predict(input);

        log.debug("Recommended Places: {}", output);

        // 장소별 점수 계산
        Map<String, List<PlaceScore>> categorizedPlaces = new HashMap<>();

        for (Place place : regionalPlaces) {
            INDArray placeLabel = dataPreprocessor.preprocessPlaceCategories(place.getCat1(), place.getCat2(), place.getCat3());

            // 복합 유사도 계산
            double score = combinedSimilarity(output, placeLabel);

            // 애완동물 동반 여부에 따른 점수 조정
            if (preferences.isPetFriendly()) {
                String acmpyTypeCd = place.getAcmpyTypeCd();
                if (acmpyTypeCd != null && acmpyTypeCd.contains("동반가능")) {
                    score += 10; // 애완동물 동반 장소 점수 증가
                } else {
                    score -= 5; // 비애완동물 장소 점수 감소
                }
            }

            // HubRank가 없는 경우 기본값 처리 (예: 0)
            String hubRankStr = place.getHubRank();
            double normalizedHubRankScore = 0.0; // 기본값

            if (hubRankStr != null && !hubRankStr.trim().isEmpty()) {
                try {
                    double hubRankScore = Double.parseDouble(hubRankStr); // 1 ~ 100 사이의 값 (1이 가장 높은 순위)
                    normalizedHubRankScore = 1.0 - (hubRankScore / 100.0); // 0 ~ 1 사이의 값으로 정규화 (1이 가장 중요한 장소)
                } catch (NumberFormatException e) {
                    // 유효하지 않은 숫자 포맷일 경우 예외 처리
                    normalizedHubRankScore = 0.0;
                }
            }

            // HubRank 점수 추가 반영
            score += normalizedHubRankScore * 0.2; // 0.2는 가중치, 필요에 따라 조정 가능


            // 카테고리별로 장소 추가
            categorizedPlaces.computeIfAbsent(place.getCat1(), k -> new ArrayList<>())
                    .add(new PlaceScore(place, score));
        }

        List<Place> topPlaces = new ArrayList<>();

        // 각 카테고리에서 상위 100개 장소 선택
        for (Map.Entry<String, List<PlaceScore>> entry : categorizedPlaces.entrySet()) {
            List<PlaceScore> scoredPlaces = entry.getValue();

            // 점수로 정렬하여 상위 100개 선택
            List<PlaceScore> topScoredPlaces = scoredPlaces.stream()
                    .sorted(Comparator.comparingDouble(PlaceScore::getScore).reversed())
                    .limit(300)
                    .collect(Collectors.toList());


            // 최종 장소 리스트에 추가
            topPlaces.addAll(topScoredPlaces.stream().map(PlaceScore::getPlace).collect(Collectors.toList()));
        }

        return topPlaces; // 최종 장소 리스트 반환
    }

    // 복합 유사도 계산 메서드
    private double combinedSimilarity(INDArray userVector, INDArray placeVector) {
        double cosineSim = cosineSimilarity(userVector, placeVector);
        double euclideanDist = euclideanDistance(userVector, placeVector);

        // 예시 가중치 설정 (조정 가능)
        double weightCosine = 0.7;
        double weightEuclidean = 0.3;

        // 복합 유사도 계산
        double combinedSim = weightCosine * cosineSim + weightEuclidean * (1.0 / (1.0 + euclideanDist));

        return combinedSim;
    }

    private double euclideanDistance(INDArray vectorA, INDArray vectorB) {
        // 두 벡터의 유클리드 거리 계산
        return Math.sqrt(Transforms.pow(vectorA.sub(vectorB), 2).sum(1).getDouble(0));
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
