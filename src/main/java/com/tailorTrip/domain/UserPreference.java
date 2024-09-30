package com.tailorTrip.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId; // 사용자 ID (Member 엔티티와 연결)
    private String purpose; // 여행 목적 (레저, 비즈니스 등)
    private String pace; // 여행 속도 (느긋하게, 빠르게)
    private String transportation; // 교통 수단 (걸어서, 차타고)
    private String interest; // 관심사 (카페, 맛집, 산책)

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 사용자와의 관계

}
