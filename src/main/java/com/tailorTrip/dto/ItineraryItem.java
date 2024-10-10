package com.tailorTrip.dto;

import com.tailorTrip.domain.Place;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItineraryItem {

    private String timeOfDay; // 아침, 오전 활동, 점심, 오후 활동, 저녁 등
    private Place place; // 방문할 장소
    private String activityType; // 활동 유형 (관광, 식사 등)
}