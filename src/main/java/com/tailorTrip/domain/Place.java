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

    private String name; // 장소 이름
    private String category; // 카테고리 (예: 카페, 맛집, 관광명소)
    private double lat; // 위도
    private double lng; // 경도
    private double rating; // 평점
    private int userRatingsTotal; // 총 평점 수
    private boolean openNow; // 현재 영업중 여부
    private String address; // 주소

//    private boolean walkable; // 도보 가능 여부
//    private boolean fastAccess; // 빠른 접근 가능 여부

    @ElementCollection
    @CollectionTable(name = "place_types", joinColumns = @JoinColumn(name = "place_id"))
    @Column(name = "type")
    private List<String> types; // 장소 유형 리스트 (예: "cafe", "restaurant")

//    private String phoneNumber; // 전화번호
//    private String website; // 웹 사이트
//    @ElementCollection
//    private List<String> photoUrls; // 사진 URL 리스트
}
