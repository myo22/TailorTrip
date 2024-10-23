package com.tailorTrip.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Map;

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
    private double mapX; // 경도
    private double mapY; // 위도
    private String tel;
    private Integer contentId;
    private String cat1; // 대분류 (자연, 인문, 레포츠 등)
    private String cat2; // 중분류 (자연관광지, 역사관광지, 휴양관광지 등)
    private String cat3; // 소분류 (산, 중식, 한식, 백화점 등)
    private String acmpyTypeCd;
    private String firstImage;
    private String firstImage2;
    private Integer areaCode;
    private Integer sigunguCode;
    private Integer contentTypeId;

    private String overview; // 개요 정보

//    @ElementCollection
//    private Map<String, Object> intro; // 추가 정보
//
//    @ElementCollection
//    private List<DetailInfo> detailInfo; // 상세 정보 (List로 Embeddable 클래스 사용)
//
//    public void updateDetailInfo(List<DetailInfo> detailInfo) {
//        this.detailInfo = detailInfo;
//    }
//
//    public void  updateIntro(Map<String, Object> intro) {
//        this.intro = intro;
//    }

    public void  updateOverview(String overview) {
        this.overview = overview;
    }
}


