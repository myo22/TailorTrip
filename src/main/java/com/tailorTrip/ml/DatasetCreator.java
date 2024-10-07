package com.tailorTrip.ml;

import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.Repository.UserPreferencesRepository;
import com.tailorTrip.domain.Place;
import com.tailorTrip.domain.UserPreferences;
import lombok.RequiredArgsConstructor;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatasetCreator {

    private final UserPreferencesRepository userPreferencesRepository;
    private final PlaceRepository placeRepository;
    private final DataPreprocessor dataPreprocessor;

    public DataSetIterator createTrainingData() {
        List<DataSet> dataSets = new ArrayList<>();

        List<UserPreferences> Preferences = userPreferencesRepository.findAll();
        List<Place> places = placeRepository.findAll();

        for(UserPreferences pref: Preferences) {
            // 사용자 선호도를 벡터로 변환
            INDArray input = dataPreprocessor.preprocessUserPreferences(pref);

//            // 예시: 관심사에 맞는 장소의 인덱스를 레이블로 설정
//            // 실제로는 다중 레이블 또는 순서 예측이 필요할 수 있음
//            Place targetPlace = places.stream()
//                    .filter(place -> place.getCategory().equalsIgnoreCase(pref.getInterest()))
//                    .findFirst()
//                    .orElse(places.get(0));
//
//            int label = getCategoryIndex(targetPlace.getCategory());

            // 실제 추천할 카테고리를 레이블로 설정 (예: interest와 연관된 카테고리)
            String interest = pref.getInterest().toLowerCase();
            int label;
            switch (interest) {
                case "카페":
                    label = 0;
                    break;
                case "맛집":
                    label = 1;
                    break;
                case "관광 명소":
                    label = 2;
                    break;
                default:
                    label = 0; // 기본값
            }

            // 원-핫 인코딩 레이블 생성
            INDArray labels = Nd4j.zeros(1, 3);
            labels.putScalar(new int[]{0, label}, 1.0);

            DataSet ds = new DataSet(input, labels);
            dataSets.add(ds);

        }

        return new ListDataSetIterator<>(dataSets, 32); // 배치 크기 설정
    }

    private int getCategoryIndex(String category) {
        switch (category.toLowerCase()) {
            case "카페":
                return 0;
            case "맛집":
                return 1;
            case "관광 명소":
                return 2;
            default:
                return 0;
        }
    }
}
