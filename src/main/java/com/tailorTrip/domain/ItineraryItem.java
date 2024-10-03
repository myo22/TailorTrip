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
public class ItineraryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String timeSlot; // 시간대 (예: "오전", "오후", "저녁")

    @ManyToOne
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary; // 속한 일정

    @ManyToOne
    @JoinColumn(name = "place_id")
    private Place place; // 추천된 장소
}