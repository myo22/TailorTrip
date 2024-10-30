package com.tailorTrip.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int dayNumber; // 몇일차

    @ManyToOne
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary; // 외래 키

    @OneToMany(mappedBy = "itineraryDay", cascade = CascadeType.ALL)
    private List<ItineraryItem> items; // 하루 일정 목록

}
