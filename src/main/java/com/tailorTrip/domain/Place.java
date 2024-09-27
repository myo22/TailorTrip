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

    private String placeId; // Google Places API의 place_id
    private String name; // 장소 이름

    private double lat; // 위도
    private double lng; // 경도

    private double raring; // 평점
    private int userRatingTotal; // 총 평점 수

    private String address; // 주소
    private String phoneNumber; // 전화번호
    private String website; // 웹 사이트

    private String openingHours; // 운영 시간 (예: "09:00-18:00")
    // 추가 필드 (예: 사진 URL 등)
}
