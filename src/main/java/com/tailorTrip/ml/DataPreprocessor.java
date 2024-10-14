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

    // 대분류 카테고리 매핑
    public static final Map<String, Integer> CATEGORY_MAP = new HashMap<>();
    // 중분류 카테고리 매핑
    public static final Map<String, Integer> SUBCATEGORY_MAP = new HashMap<>();
    // 소분류 카테고리 매핑
    public static final Map<String, Integer> DETAIL_CATEGORY_MAP = new HashMap<>();

    static {
        // 대분류
        CATEGORY_MAP.put("A01", 0); // 자연
        CATEGORY_MAP.put("A02", 1); // 인문(문화/예술/역사)
        CATEGORY_MAP.put("A03", 2); // 레포츠
        CATEGORY_MAP.put("B02", 3); // 숙박
        CATEGORY_MAP.put("A04", 4); // 쇼핑
        CATEGORY_MAP.put("A05", 5); // 음식

        // 중분류
        SUBCATEGORY_MAP.put("A0101", 0); // 자연관광지
        SUBCATEGORY_MAP.put("A0102", 1); // 관광자원
        SUBCATEGORY_MAP.put("A0201", 2); // 역사관광지
        SUBCATEGORY_MAP.put("A0202", 3); // 휴양관광지
        SUBCATEGORY_MAP.put("A0203", 4); // 체험관광지
        SUBCATEGORY_MAP.put("A0204", 5); // 산업관광지
        SUBCATEGORY_MAP.put("A0205", 6); // 건축/조형물
        SUBCATEGORY_MAP.put("A0206", 7); // 문화시설
        SUBCATEGORY_MAP.put("A0207", 8); // 축제
        SUBCATEGORY_MAP.put("A0208", 9); // 공연/행사
        SUBCATEGORY_MAP.put("A0301", 10); // 레포츠 소개
        SUBCATEGORY_MAP.put("A0302", 11); // 육상 레포츠
        SUBCATEGORY_MAP.put("A0303", 12); // 수상 레포츠
        SUBCATEGORY_MAP.put("A0304", 13); // 항공 레포츠
        SUBCATEGORY_MAP.put("A0305", 14); // 복합 레포츠
        SUBCATEGORY_MAP.put("B0201", 15); // 숙박시설
        SUBCATEGORY_MAP.put("A0401", 16); // 쇼핑
        SUBCATEGORY_MAP.put("A0502", 17); // 음식점

        // 소분류
        DETAIL_CATEGORY_MAP.put("B02010100", 0); // 관광호텔
        DETAIL_CATEGORY_MAP.put("B02010500", 1); // 콘도미니엄
        DETAIL_CATEGORY_MAP.put("B02010600", 2); // 유스호스텔
        DETAIL_CATEGORY_MAP.put("B02010700", 3); // 펜션
        DETAIL_CATEGORY_MAP.put("B02010900", 4); // 모텔
        DETAIL_CATEGORY_MAP.put("B02011000", 5); // 민박
        DETAIL_CATEGORY_MAP.put("B02011100", 6); // 게스트하우스
        DETAIL_CATEGORY_MAP.put("B02011200", 7); // 홈스테이
        DETAIL_CATEGORY_MAP.put("B02011300", 8); // 서비스드레지던스
        DETAIL_CATEGORY_MAP.put("B02011600", 9); // 한옥
        DETAIL_CATEGORY_MAP.put("A05020100", 10); // 한식
        DETAIL_CATEGORY_MAP.put("A05020200", 11); // 양식
        DETAIL_CATEGORY_MAP.put("A05020300", 12); // 일식
        DETAIL_CATEGORY_MAP.put("A05020400", 13); // 중식
        DETAIL_CATEGORY_MAP.put("A05020900", 14); // 카페

    }

    // 사용자 선호도를 벡터로 변환
    public INDArray preprocessUserPreferences(UserPreferences prefs) {
        float[] features = new float[4]; // 특성 벡터 크기 축소

        // 특정 관심사 인코딩 (대분류)
        Integer purposeIndex = CATEGORY_MAP.get(prefs.getInterest());
        features[0] = purposeIndex != null ? purposeIndex : -1; // 카테고리 매핑이 없으면 -1

        // 특정 활동 스타일 인코딩 (중분류)
        Integer paceIndex = SUBCATEGORY_MAP.get(prefs.getActivityType());
        features[1] = paceIndex != null ? paceIndex : -1; // 카테고리 매핑이 없으면 -1

        // 선호하는 음식 인코딩 (소분류)
        Integer foodIndex = DETAIL_CATEGORY_MAP.get(prefs.getFoodPreference());
        features[2] = foodIndex != null ? foodIndex : -1; // 카테고리 매핑이 없으면 -1

        // 숙소 인코딩 (소분류)
        Integer accommodationIndex = DETAIL_CATEGORY_MAP.get(prefs.getAccommodationPreference());
        features[3] = accommodationIndex != null ? accommodationIndex : -1; // 카테고리 매핑이 없으면 -1

//        여기서 1D 배열을 2D 배열로 변환
        return Nd4j.create(new float[][]{features});
    }

    public INDArray preprocessPlaceCategories(String cat1, String cat2, String cat3) {
        // 각 카테고리별로 원-핫 인코딩을 별도로 수행하고, 합침
        int totalCategories = CATEGORY_MAP.size() + SUBCATEGORY_MAP.size() + DETAIL_CATEGORY_MAP.size();
        float[] labels = new float[totalCategories];

        // 대분류
        Integer cat1Index = CATEGORY_MAP.get(cat1);
        if (cat1Index != null) {
            labels[cat1Index] = 1.0f;
        }

        // 중분류
        Integer cat2Index = SUBCATEGORY_MAP.get(cat2);
        if (cat2Index != null) {
            labels[CATEGORY_MAP.size() + cat2Index] = 1.0f;
        }

        // 소분류
        Integer cat3Index = DETAIL_CATEGORY_MAP.get(cat3);
        if (cat3Index != null) {
            labels[CATEGORY_MAP.size() + SUBCATEGORY_MAP.size() + cat3Index] = 1.0f;
        }

        // 1D 배열을 2D 배열로 변환 (1행, N열)
        return Nd4j.create(new float[][]{labels});
    }

    // 39차원 제로 레이블 생성
    public INDArray createZeroLabel() {
        int totalCategories = CATEGORY_MAP.size() + SUBCATEGORY_MAP.size() + DETAIL_CATEGORY_MAP.size(); // 6 + 18 + 15 = 39
        return Nd4j.zeros(new int[]{1, totalCategories});
    }
}