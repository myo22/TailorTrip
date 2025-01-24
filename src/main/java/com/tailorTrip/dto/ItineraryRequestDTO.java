package com.tailorTrip.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryRequestDTO {
    private String title;
    private String info;
    private String category;
    private String url;
    private String contenttypeid;
}
