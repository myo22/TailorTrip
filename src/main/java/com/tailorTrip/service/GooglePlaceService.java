package com.tailorTrip.service;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlacesSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GooglePlaceService {

    private final GeoApiContext context;

    public GooglePlaceService(@Value("${google.api.key}") String apiKey) {
        this.context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public PlacesSearchResponse searchPlaces(String query, String location) throws Exception {
        return PlacesApi.textSearchQuery(context, query)
                .location(location)
                .radius(1000) // 1km 범위 내 검색
                .await();
    }
}
