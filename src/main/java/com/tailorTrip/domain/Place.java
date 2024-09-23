package com.tailorTrip.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 장소 이름
    private String category; // 카테고리 (카페, 맛집, 관광명소)
    private double lat; // 위도
    private double lng; // 경도

    private boolean walkable; // 도보 가능 여부
    private boolean fastAccess; // 빠른 접근 가능 여부
}
