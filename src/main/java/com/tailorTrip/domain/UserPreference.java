package com.tailorTrip.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String purpose; // 여행 목적 (레저, 비즈니스 등)
    private String pace; // 여행 속도 (느긋하게, 빠르게)
    private String transportation; // 교통 수단 (걸어서, 차타고)
    private String interest; // 관심사 (카페, 맛집, 산책)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member; // 사용자와의 관계

}
