package com.tailorTrip.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import com.tailorTrip.domain.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GooglePlacesService {

    @Value("${google.api.key}")
    private String apiKey;



    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public Place enrichPlaceWithDetails(Place place) {
        try {
            String query = place.getTitle() + " " + place.getAddr1();
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());

            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query="
                    + encodedQuery + "&key=" + apiKey;

            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");

            if (results.isArray() && results.size() > 0) {
                JsonNode firstResult = results.get(0);

                // 평점 및 평점 수
                double rating = firstResult.path("rating").asDouble(0.0);
                int userRatingsTotal = firstResult.path("user_ratings_total").asInt(0);
                place.updateRating(rating);
                place.updateRatingTotal(userRatingsTotal);

                // 가격 수준
                int priceLevel = firstResult.path("price_level").asInt(0);
                place.updatePriceLevel(priceLevel);

                // 오픈 시간
                JsonNode openingHours = firstResult.path("opening_hours");
                if (!openingHours.isMissingNode()) {
                    String hours = openingHours.toString(); // JSON 형태로 저장
                    place.updateOpeningHours(hours);
                }

                // 웹사이트
                String website = firstResult.path("website").asText(null);
                place.updateWebsite(website);

                // 사진 참조 ID
                JsonNode photos = firstResult.path("photos");
                if (photos.isArray() && photos.size() > 0) {
                    String photoReference = photos.get(0).path("photo_reference").asText(null);
                    place.updatePhotoReference(photoReference);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return place;
    }

    // 추가적으로 사진 URL을 가져오는 메서드
    public String fetchPhotoUrl(String photoReference, int maxWidth) {
        try {
            String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth="
                    + maxWidth + "&photoreference=" + photoReference + "&key=" + apiKey;

            // 실제로 이미지는 리다이렉트되므로, URL을 직접 저장하거나 다운로드할 수 있음
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}