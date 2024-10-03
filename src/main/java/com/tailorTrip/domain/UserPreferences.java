package com.tailorTrip.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String purpose; // 여행 목적 (레저, 비즈니스 등)
    private String pace; // 여행 스타일 (느긋하게, 빠르게 등)
    private String transportation; // 이동 수단 (도보, 자전거, 자동차, 대중교통)
    private String interest; // 관심사 (카페, 맛집, 산책 등)
    private String duration; // 여행 기간 (하루, 주말, 일주일)
    private String budget; // 여행 예산 (저렴한, 중간, 고급)


}
