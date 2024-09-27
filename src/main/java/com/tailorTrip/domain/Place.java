package com.tailorTrip.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String placeId; // Google Places API의 place_id
    private String name; // 장소 이름

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // 카테고리 (예: 카페, 맛집, 관광명소)

    private double lat; // 위도
    private double lng; // 경도

    private double rating; // 평점
    private int userRatingTotal; // 총 평점 수

    private String address; // 주소
//    private String phoneNumber; // 전화번호
//    private String website; // 웹 사이트

    private String openingHours; // 운영 시간 (예: "09:00-18:00")

    private boolean walkable; // 도보 가능 여부
    private boolean fastAccess; // 빠른 접근 가능 여부

    @ElementCollection
    private List<String> photoUrls; // 사진 URL 리스트
}
