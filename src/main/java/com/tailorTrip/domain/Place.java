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

    private String title; // 장소 이름
    private String addr1; // 주소
    private String addr2;
    private String zipcode;
    private double mapx; // 경도
    private double mapy; // 위도
    private String tel;
    private Integer contentid;
    private String cat1; // 대분류 (자연, 인문, 레포츠 등)
    private String cat2; // 중분류 (자연관광지, 역사관광지, 휴양관광지 등)
    private String cat3; // 소분류 (산, 중식, 한식, 백화점 등)
    private String acmpyTypeCd;
    private String firstimage;
    private String firstimage2;
    private Integer areacode;
    private Integer sigungucode;
    private Integer contentTypeId;

    private Double rating; // 평점 추가
    private Integer userRatingsTotal; // 평점 수
    private Integer priceLevel; // 가격 수준
    private String openingHours; // 오픈 시간 (JSON 형식 또는 문자열)
    private String website; // 웹사이트 URL

    public void updateRating(double rating) {
        this.rating = rating;
    }


    public void updateRatingTotal(int userRatingsTotal) {
        this.userRatingsTotal = userRatingsTotal;
    }


    public void updatePriceLevel(int priceLevel) {
        this.priceLevel = priceLevel;
    }


    public void updateOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }


    public void updateWebsite(String website) {
        this.website = website;
    }

}


