package com.tailorTrip.service;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import com.tailorTrip.Repository.CategoryRepository;
import com.tailorTrip.domain.Category;
import com.tailorTrip.domain.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleMapsService {

    @Value("${google.api.key}")
    private String apiKey;

    private final CategoryRepository categoryRepository;


    private GeoApiContext getGeoContext() {
        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public List<Place> fetchPlaces(String location, String category) {
        GeoApiContext context = getGeoContext();
        List<Place> places = new ArrayList<>();

        try {
            PlacesSearchResponse response = PlacesApi.textSearchQuery(context, category + " in " + location)
                    .await();

            for (PlacesSearchResult result : response.results) {
                Place place = Place.builder()
                        .placeId(result.placeId)
                        .name(result.name)
                        .category(getCategoryName(category))
                        .lat(result.geometry.location.lat)
                        .lng(result.geometry.location.lng)
                        .rating(result.rating != null ? result.rating : 0.0)
                        .userRatingsTotal(result.userRatingsTotal != null ? result.userRatingsTotal : 0)
                        .address(result.formattedAddress)
                        .openingHours(getOpeningHours(result))
                        .walkable(isWalkable(category))
                        .fastAccess(isFastAccess(category))
                        .build();
                places.add(place);
            }
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
        }

        return places;
    }

    private String getCategoryName(String category) {
        // 필요에 따라 카테고리 이름 변환 로직 구현
        return category;
    }

    private String getOpeningHours(PlacesSearchResult result) {
        if(result.openingHours != null && result.openingHours.weekdayText != null) {
            // 예시: '월요일: 09:00-18:00" 형태로 반환해주는 것.
            return String.join(", ", result.openingHours.weekdayText);
        }
        return "운영 시간 정보 없음";
    }

    private boolean isWalkable(String category) {
        // 카테고리에 따라 도보 가능 여부 설정
        switch (category.toLowerCase()) {
            case "카페":
            case "산책":
            case "관광명소":
                return true;
            case "맛집":
                return false;
            default:
                return false;
        }
    }

    private boolean isFastAccess(String category) {
        // 카테고리에 따라 빠른 접근 가능 여부 설정
        switch (category.toLowerCase()) {
            case "카페":
            case "맛집":
                return true;
            case "산책":
            case "관광명소":
                return false;
            default:
                return false;
        }
    }

}
