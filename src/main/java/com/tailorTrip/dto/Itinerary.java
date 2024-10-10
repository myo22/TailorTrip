package com.tailorTrip.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
public class Itinerary {

    private int duration; // 여행 기간 (예: 1박, 2박)
    private List<ItineraryDay> days;

}
