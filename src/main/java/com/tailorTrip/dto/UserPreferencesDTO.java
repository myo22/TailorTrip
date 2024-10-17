package com.tailorTrip.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserPreferencesDTO {
    private String region;
    private List<String> interests;
    private List<String> activities;
    private boolean petFriendly;
    private List<String> foodPreferences;
    private String travelPace;
    private String accommodationPreference;
    private int tripDuration;
    private double userLat;
    private double userLng;
}

