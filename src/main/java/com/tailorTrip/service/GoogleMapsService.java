package com.tailorTrip.service;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogleMapsService {

    private final GeoApiContext context;

    public GoogleMapsService(@Value("${google.api.key}") String apiKey) {
        this.context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public DirectionsResult getDirections(String origin, String destination) throws Exception {
        return DirectionsApi.newRequest(context)
                .mode(TravelMode.DRIVING) // 운전 모드, 필요에 따라 변경 가능
                .origin(origin)
                .destination(destination)
                .await();
    }
}
