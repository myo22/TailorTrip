package com.tailorTrip.initializer;

import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.Repository.UserPreferencesRepository;
import com.tailorTrip.domain.Place;
import com.tailorTrip.domain.UserPreferences;
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
public class DatasetCreator {

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private DataPreprocessor dataPreprocessor;

    public DataSetIterator createTrainingData() {
        List<DataSet> dataSets = new ArrayList<>();

        List<UserPreferences> userPreferences = userPreferencesRepository.findAll();
        List<Place> places = placeRepository.findAll();

        for(UserPreferences pref: userPreferences) {
            INDArray input = dataPreprocessor.preprocessUserPreferences(pref);

            // 예시: 관심사에 맞는 장소의 인덱스를 레이블로 설정
            // 실제로는 다중 레이블 또는 순서 예측이 필요할 수 있음
            Place targetPlace = places.stream()
                    .filter(place -> place.getCategory().equalsIgnoreCase(pref.getInterest()))
                    .findFirst()
                    .orElse(places.get(0));

            int label = getCategoryIndex(targetPlace.getCategory());

            INDArray labels = Nd4j.zeros(1, 3); // 카테고리 수에 맞게 조정
            labels.putScalar(new int[]{0, label}, 1.0);

            DataSet ds = new DataSet(input, labels);
                dataSets.add(ds);
        }

        return new ListDataSetIterator<>(dataSets, 10);
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
