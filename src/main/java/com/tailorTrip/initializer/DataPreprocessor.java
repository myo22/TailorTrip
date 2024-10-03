package com.tailorTrip.initializer;

import com.tailorTrip.Repository.MemberRepository;
import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.Member;
import com.tailorTrip.domain.Place;
import com.tailorTrip.domain.UserPreferences;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataPreprocessor {

    // 예시: 간단한 인코딩 방법
    public INDArray preprocessUserPreferences(UserPreferences prefs) {

        double[] features = new double[6];

        // 여행 목적
        switch (prefs.getPurpose()) {
            case "레저":
                features[0] = 1;
                break;
            case "비즈니스":
                features[0] = 2;
                break;
            case "탐험":
                features[0] = 3;
                break;
            case "문화 체험":
                features[0] = 4;
                break;
            default:
                features[0] = 0;
        }
        // 여행 스타일
        switch (prefs.getPace()) {
            case "느긋하게":
                features[1] = 1;
                break;
            case "빠르게":
                features[1] = 2;
                break;
            case "모험적으로":
                features[1] = 3;
                break;
            case "편안하게":
                features[1] = 4;
                break;
            default:
                features[1] = 0;
        }

        // 이동 수단
        switch (prefs.getTransportation()) {
            case "도보":
                features[2] = 1;
                break;
            case "자전거":
                features[2] = 2;
                break;
            case "자동차":
                features[2] = 3;
                break;
            case "대중교통":
                features[2] = 4;
                break;
            default:
                features[2] = 0;
        }

        // 관심사
        switch (prefs.getInterest()) {
            case "카페":
                features[3] = 1;
                break;
            case "맛집":
                features[3] = 2;
                break;
            case "산책":
                features[3] = 3;
                break;
            case "관광 명소":
                features[3] = 4;
                break;
            case "자연 경관":
                features[3] = 5;
                break;
            case "박물관":
                features[3] = 6;
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

        // 여행 예산
        switch (prefs.getBudget()) {
            case "저렴한":
                features[5] = 1;
                break;
            case "중간 가격대":
                features[5] = 2;
                break;
            case "고급":
                features[5] = 3;
                break;
            default:
                features[5] = 0;
        }

        return Nd4j.create(features);
    }
}