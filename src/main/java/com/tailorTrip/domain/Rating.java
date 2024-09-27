package com.tailorTrip.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId; // 사용자 ID (익명 사용자일 경우 적절한 식별자 사용)

    private Long placeId; // 해당 평점을 남긴 장소의 ID

    private double score; // 평점
    private String comment; // 평점에 대한 코멘트

}
