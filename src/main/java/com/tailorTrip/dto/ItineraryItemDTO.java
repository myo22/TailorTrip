package com.tailorTrip.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryItemDTO {
    private String timeOfDay;
    private String activityType;

}
