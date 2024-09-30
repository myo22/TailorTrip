package com.tailorTrip.service;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import com.tailorTrip.domain.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GooglePlacesService {

    @Value("${google.api.key}")
    private String apiKey;

    private final CategoryRepository categoryRepository;


    private GeoApiContext getGeoContext() {
        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public List<Place> fetchPlaces(String location, String type) {
        GeoApiContext context = getGeoContext();
        List<Place> places = new ArrayList<>();

        try {
            PlacesSearchResponse response = PlacesApi.textSearchQuery(context, type + " in " + location)
                    .await();

            for (PlacesSearchResult result : response.results) {
                Place place = Place.builder()
                        .name(result.name)
                        .category(type)
                        .lat(result.geometry.location.lat)
                        .lng(result.geometry.location.lng)
                        .rating(result.rating != null ? result.rating : 0.0)
                        .userRatingsTotal(result.userRatingsTotal != null ? result.userRatingsTotal : 0)
                        .openNow(result.openingHours != null && result.openingHours.openNow)
                        .address(result.formattedAddress)
                        .types(List.of(result.types))
                        .build();
                places.add(place);
            }
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
        }

        return places;
    }


    private String getOpeningHours(PlacesSearchResult result) {
        if(result.openingHours != null && result.openingHours.weekdayText != null) {
            // 예시: '월요일: 09:00-18:00" 형태로 반환해주는 것.
            return String.join(", ", result.openingHours.weekdayText);
        }
        return "운영 시간 정보 없음";
    }

}
