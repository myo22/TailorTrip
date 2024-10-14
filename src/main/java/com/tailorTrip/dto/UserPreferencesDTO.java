package com.tailorTrip.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserPreferencesDTO {
    private String region;
    private String interests;
    private String activities;
    private boolean petFriendly;
    private String foodPreferences;
    private String travelPace;
    private String accommodationPreference;
    private int tripDuration;
    private double userLat;
    private double userLng;
}

