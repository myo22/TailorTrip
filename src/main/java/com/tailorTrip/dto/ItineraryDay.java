package com.tailorTrip.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ItineraryDay {

    private int dayNumber; // 몇일차
    private List<ItineraryItem> items; // 하루 일정 목록

}
