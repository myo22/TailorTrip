package com.tailorTrip.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserPreferencesDTO {
    private String region;
    private int tripDuration;
    private List<String> interests;
    private List<String> activities;
    private List<String> foodPreferences;
    private boolean petPreference;
    private String travelStyle;
    private List<String> accommodation;
}

