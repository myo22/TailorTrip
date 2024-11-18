package com.tailorTrip.ml;

import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.Repository.UserPreferencesRepository;
import com.tailorTrip.domain.Place;
import com.tailorTrip.domain.UserPreferences;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DatasetCreator {

    private final UserPreferencesRepository userPreferencesRepository;
    private final PlaceRepository placeRepository;
    private final DataPreprocessor dataPreprocessor;

    @Transactional
    public DataSetIterator createTrainingData() {
        List<DataSet> dataSets = new ArrayList<>();

        List<UserPreferences> preferences = userPreferencesRepository.findAll();
        List<Place> places = placeRepository.findAll();

        for (UserPreferences pref : preferences) {
            // 사용자 선호도를 벡터로 변환
            INDArray input = dataPreprocessor.preprocessUserPreferences(pref);

            // 사용자의 중분류, 소분류 기준으로 장소 카테고리 설정
            List<String> userCategories = getUserCategories(pref); // 중분류 기준으로
            List<String> userSpecificCategories = getUserSpecificCategories(pref); // 소분류 기준으로

            // 긍정적인 샘플 추가
            List<Place> positivePlaces = getPositivePlaces(places, userCategories, userSpecificCategories);
            for (Place place : positivePlaces) {
                INDArray labels = dataPreprocessor.preprocessPlaceCategories(place.getCat1(), place.getCat2(), place.getCat3());
                DataSet ds = new DataSet(input, labels);
                dataSets.add(ds);
            }

            // 부정적인 샘플 추가 (관련 없는 장소 중 일부 선택)
            List<Place> negativePlaces = getNegativePlaces(places, positivePlaces, 10); // 각 사용자당 10개의 부정 샘플
            for (Place place : negativePlaces) {
                INDArray labels = dataPreprocessor.createZeroLabel(); // 39차원 제로 레이블
                DataSet ds = new DataSet(input, labels);
                dataSets.add(ds);
            }
        }

        return new ListDataSetIterator<>(dataSets, 32); // 배치 사이즈 설정
    }

    private List<String> getUserCategories(UserPreferences pref) {

        if (pref == null) {
            // pref가 null인 경우 처리
            System.out.println("UserPreferences 객체가 null입니다.");
            // 빈 리스트를 반환하거나 적절한 예외 처리
            return Collections.emptyList();
        }

        List<String> userCategories = new ArrayList<>();

        // 관심사 리스트를 반복문으로 처리
        if (pref.getInterest() != null) {
            for (String interest : pref.getInterest()) {
                switch (interest) {
                    case "자연":
                        userCategories.add("A0101"); // 자연관광지
                        break;
                    case "역사":
                        userCategories.add("A0201"); // 역사관광지
                        break;
                    case "휴양":
                        userCategories.add("A0202"); // 휴양관광지
                        break;
                    case "체험":
                        userCategories.add("A0203"); // 체험관광지
                        break;
                    case "건축/조형물":
                        userCategories.add("A0205"); // 건축/조형물
                        userCategories.add("A0204"); // 산업관광지
                        break;
                    default:
                        break;
                }
            }
        }

        // 활동 스타일 리스트를 반복문으로 처리
        if (pref.getActivityType() != null) {
            for (String activityType : pref.getActivityType()) {
                switch (activityType) {
                    case "레포츠":
                        userCategories.add("A0301"); // 레포츠 소개
                        userCategories.add("A0302"); // 육상 레포츠
                        userCategories.add("A0303"); // 수상 레포츠
                        userCategories.add("A0304"); // 항공 레포츠
                        userCategories.add("A0305"); // 복합 레포츠
                        break;
                    case "공연/행사":
                        userCategories.add("A0208"); // 공연/행사
                        break;
                    case "쇼핑":
                        userCategories.add("A0401"); // 쇼핑
                        break;
                    case "문화시설":
                        userCategories.add("A0206"); // 문화시설
                        break;
                    default:
                        break;
                }
            }
        }
        return userCategories;
    }

    // 소분류 기준 구체적 활동 (숙소, 음식 등)
    private List<String> getUserSpecificCategories(UserPreferences pref) {
        List<String> specificCategories = new ArrayList<>();
        if (pref == null) {
            System.out.println("UserPreferences is null.");
            return specificCategories; // early return
        }

        // 숙소 선호 리스트를 처리
        if (pref.getAccommodationPreference() != null) {
            for (String accommodationPref : pref.getAccommodationPreference()) {
                addAccommodationCategories(accommodationPref, specificCategories);
            }
        }

        // 음식 선호 리스트를 처리
        if (pref.getFoodPreference() != null) {
            for (String foodPref : pref.getFoodPreference()) {
                addFoodCategories(foodPref, specificCategories);
            }
        }

        return specificCategories;
    }

    private void addAccommodationCategories(String accommodationPref, List<String> specificCategories) {
        if (accommodationPref != null) {
            switch (accommodationPref) {
                case "호텔":
                    specificCategories.add("B02010100"); // 관광호텔
                    specificCategories.add("B02010500"); // 콘도미니엄
                    specificCategories.add("B02011300"); // 서비스드레지던스
                    break;
                case "모텔":
                    specificCategories.add("B02010900"); // 모텔
                    break;
                case "펜션":
                    specificCategories.add("B02010700"); // 펜션
                    break;
                case "민박":
                    specificCategories.add("B02011000"); // 민박
                    specificCategories.add("B02010600"); // 유스호스텔
                    specificCategories.add("B02011100"); // 게스트하우스
                    specificCategories.add("B02011200"); // 홈스테이
                    specificCategories.add("B02011600"); // 한옥
                    break;
                default:
                    break;
            }
        } else {
            System.out.println("Accommodation preference is null.");
        }
    }

    private void addFoodCategories(String foodPref, List<String> specificCategories) {
        if (foodPref != null) {
            switch (foodPref) {
                case "한식":
                    specificCategories.add("A05020100"); // 한식
                    break;
                case "양식":
                    specificCategories.add("A05020200"); // 양식
                    break;
                case "중식":
                    specificCategories.add("A05020400"); // 중식
                    break;
                case "일식":
                    specificCategories.add("A05020300"); // 일식
                    break;
                default:
                    break;
            }
        } else {
            System.out.println("Food preference is null.");
        }
    }

    // 긍정적인 샘플을 위한 장소 필터링
    private List<Place> getPositivePlaces(List<Place> places, List<String> userCategories, List<String> userSpecificCategories) {
        return places.stream()
                .filter(place -> userCategories.contains(place.getCat2()) || userSpecificCategories.contains(place.getCat3()))
                .collect(Collectors.toList());
    }

    // 부정적인 샘플을 위한 장소 필터링
    private List<Place> getNegativePlaces(List<Place> allPlaces, List<Place> positivePlaces, int count) {
        return allPlaces.stream()
                .filter(place -> !positivePlaces.contains(place))
                .collect(Collectors.toList())
                .stream()
                .limit(count)
                .collect(Collectors.toList());
    }
}