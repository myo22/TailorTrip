package com.tailorTrip.ml;

import com.tailorTrip.domain.UserPreferences;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class DataPreprocessor {

    // 카테고리 매핑 (예: "A04" -> 0, "A0502" -> 1, ...)
    static final Map<String, Integer> CATEGORY_MAP = new HashMap<>();

    static {
        // 모든 카테고리를 매핑
        CATEGORY_MAP.put("A04", 0);
        CATEGORY_MAP.put("A0401", 1);
        CATEGORY_MAP.put("A04010600", 2);
        CATEGORY_MAP.put("A05", 3);
        CATEGORY_MAP.put("A0502", 4);
        CATEGORY_MAP.put("A05020300", 5);
        CATEGORY_MAP.put("A02", 6);
        CATEGORY_MAP.put("A0206", 7);
        CATEGORY_MAP.put("A02061000", 8);
        // 필요에 따라 추가
    }

    // 사용자 선호도를 벡터로 변환
    public INDArray preprocessUserPreferences(UserPreferences prefs) {
        double[] features = new double[6];

        // 특정 관심사 인코딩
        switch (prefs.getPurpose()) {
            case "자연":
                features[0] = 1;
                break;
            case "역사":
                features[0] = 2;
                break;
            case "휴양":
                features[0] = 3;
                break;
            case "체험":
                features[0] = 4;
                break;
            case "건축/조형물":
                features[0] = 5;
                break;
            default:
                features[0] = 0;
        }

        // 특정 활동 스타일
        switch (prefs.getPace()) {
            case "문화시설":
                features[1] = 1;
                break;
            case "공연/행사":
                features[1] = 2;
                break;
            case "레포츠":
                features[1] = 3;
                break;
            case "쇼핑":
                features[1] = 4;
                break;
            default:
                features[1] = 0;
        }

        // 여행 스타일 인코딩
        switch (prefs.getTransportation()) {
            case "느긋하게":
                features[1] = 1;
                break;
            case "보통":
                features[1] = 2;
                break;
            case "바쁘게":
                features[1] = 3;
                break;
            default:
                features[1] = 0;
        }

        // 선호하는 음식 인코딩
        switch (prefs.getInterest()) {
            case "한식":
                features[3] = 1;
                break;
            case "양식":
                features[3] = 2;
                break;
            case "일식":
                features[3] = 3;
                break;
            case "중식":
                features[3] = 4;
                break;
            default:
                features[3] = 0;
        }

        // 여행 기간
        switch (prefs.getDuration()) {
            case "하루":
                features[4] = 1;
                break;
            case "주말":
                features[4] = 2;
                break;
            case "일주일":
                features[4] = 3;
                break;
            default:
                features[4] = 0;
        }

        // 숙소 인코딩
        switch (prefs.getBudget()) {
            case "호텔":
                features[5] = 1;
                break;
            case "펜션":
                features[5] = 2;
                break;
            case "모텔":
                features[5] = 3;
                break;
            case "민박":
                features[5] = 4;
                break;
            default:
                features[5] = 0;
        }

//        여기서 1D 배열을 2D 배열로 변환
        return Nd4j.create(new double[][]{features});
    }

    // 장소 카테고리를 멀티레이블 원-핫 인코딩으로 변환
    public INDArray preprocessPlaceCategories(String cat1, String cat2, String cat3) {
        double[] labels = new double[CATEGORY_MAP.size()];
        Set<String> categories = new HashSet<>();
        categories.add(cat1);
        categories.add(cat2);
        categories.add(cat3);

        for (String cat : categories) {
            Integer index = CATEGORY_MAP.get(cat);
            if (index != null) {
                labels[index] = 1.0;
            }
        }

        return Nd4j.create(labels);
    }
}