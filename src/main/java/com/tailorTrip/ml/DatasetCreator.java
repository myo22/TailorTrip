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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.tailorTrip.ml.DataPreprocessor.CATEGORY_MAP;

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

            // 해당 사용자의 선호도에 맞는 장소 카테고리를 찾습니다.
            // 예를 들어, 관심사에 해당하는 카테고리를 찾습니다.


            List<String> userCategories = new ArrayList<>();
            switch (pref.getInterest()) {
                case "자연":
                    userCategories.add("A04");
                    userCategories.add("A0401");
                    userCategories.add("A04010600");
                    break;
                case "역사":
                    userCategories.add("A05");
                    userCategories.add("A0502");
                    userCategories.add("A05020300");
                    break;
                case "휴양":
                    userCategories.add("A02");
                    userCategories.add("A0206");
                    userCategories.add("A02061000");
                    break;
                case "체험":
                    userCategories.add("A06");
                    // 추가 카테고리
                    break;
                case "건축/조형물":
                    userCategories.add("A07");
                    // 추가 카테고리
                    break;
                default:
                    break;
            }

            // 해당 카테고리에 속하는 장소의 카테고리를 레이블로 설정
            Set<String> categories = new HashSet<>();
            for (String cat : userCategories) {
                categories.add(cat);
            }

            // 레이블 생성 (멀티레이블)
            INDArray labels = Nd4j.zeros(CATEGORY_MAP.size());


            for (Place place : places) {
                if (categories.contains(place.getCat1()) ||
                        categories.contains(place.getCat2()) ||
                        categories.contains(place.getCat3())) {
                    INDArray placeLabels = dataPreprocessor.preprocessPlaceCategories(place.getCat1(), place.getCat2(), place.getCat3());
                    labels = labels.add(placeLabels);
                }
            }

            // 이진화 (0 또는 1)
            for (int i = 0; i < labels.length(); i++) {
                if (labels.getDouble(i) > 0) {
                    labels.putScalar(i, 1.0);
                } else {
                    labels.putScalar(i, 0.0);
                }
            }

            DataSet ds = new DataSet(input, labels);
            dataSets.add(ds);

        }

        return new ListDataSetIterator<>(dataSets, 32); // 배치 사이즈 설정
    }

}
