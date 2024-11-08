package com.tailorTrip.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String region;

    @ElementCollection
    private List<String> interest; // 관심사 (역사, 자연, 휴양 등)

    @ElementCollection
    private List<String> activityType; // 여행 활동 (레저, 쇼핑, 문화시설 등)

    private boolean petFriendly;

    @ElementCollection
    private List<String> foodPreference; // 선호 음식 (한식, 중식, 일식, 양식)

    private String travelPace; // 여행 스타일 (느긋하게, 빠르게 등)

    @ElementCollection
    private List<String> accommodationPreference; // 숙소 (민박, 펜션 등)

    private int tripDuration; // 여행 기간

    private double userLat;       // 사용자

    private double userLng;       // 사용자 경도

}
