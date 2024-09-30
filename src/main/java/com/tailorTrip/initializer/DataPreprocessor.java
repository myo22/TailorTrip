package com.tailorTrip.initializer;

import com.tailorTrip.Repository.MemberRepository;
import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.Member;
import com.tailorTrip.domain.Place;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataPreprocessor {

    private final MemberRepository memberRepository;

    private final PlaceRepository placeRepository;

    public List<UserFeature> preprocessData() {

        List<Member> members = memberRepository.findAll();
        List<Place> places = placeRepository.findAll();

        List<UserFeature> userFeatures = new ArrayList<>();

        for(Member member : members) {
            for(UserPreference preference : member.getPreferences()) {
                UserFeature feature = new UserFeature();
                feature.setPurpose(mapPurpose(preference.getPurpose()));
                feature.setPurpose(mapPace(preference.getPace()));
                feature.setTransportation(mapTransportation(preference.getTransportation()));
                feature.setInterest(mapInterest(preference.getInterest()));
                // 사용자 선호도에 따라 장소와의 연관성 점수 추가 (예: 좋아하는 카테고리의 장소)
                feature.setFavoritePlace(places.stream()
                        .filter(place -> place.getCategory().equals(preference.getInterest()))
                        .map(Place::getId)
                        .collect(Collectors.toList()));
                userFeatures.add(feature);
            }
        }
    }

    private double mapPurpose(String purpose) {
        switch (purpose.toLowerCase()) {
            case "레저":
                return 1.0;
            case "비즈니스":
                return 0.0;
            default:
                return 0.5;
        }
    }

    private double mapPace(String pace) {
        switch (pace.toLowerCase()) {
            case "느긋하게":
                return 0.0;
            case "빠르게":
                return 1.0;
            default:
                return 0.5;
        }
    }

    private double mapTransportation(String transportation) {
        switch (transportation.toLowerCase()) {
            case "걸어서":
                return 0.0;
            case "차타고":
                return 1.0;
            default:
                return 0.5;
        }
    }

    private double mapInterest(String interest) {
        switch (interest.toLowerCase()) {
            case "카페":
                return 1.0;
            case "맛집":
                return 0.8;
            case "산책":
                return 0.6;
            default:
                return 0.5;
        }
    }
}

@Data
class UserFeature {
    private double purpose;
    private double pace;
    private double transportation;
    private double interest;
    private List<Long> favoritePlace;
}
