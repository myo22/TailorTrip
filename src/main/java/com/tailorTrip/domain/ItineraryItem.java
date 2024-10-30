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

    private String timeOfDay; // 아침, 점심 등

    @ManyToOne
    @JoinColumn(name = "itinerary_day_id")
    private ItineraryDay itineraryDay; // 외래 키

    private String activityType; // 활동 유형

    @ManyToOne
    @JoinColumn(name = "place_id")
    private Place place; // 방문할 장소

}
