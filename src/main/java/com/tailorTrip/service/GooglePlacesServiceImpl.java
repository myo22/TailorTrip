package com.tailorTrip.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tailorTrip.domain.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class GooglePlacesServiceImpl implements GooglePlacesService {

    @Value("${google.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = "placeRatings", key = "#place.id")
    public Place enrichPlaceWithDetails(Place place) {
        try {
            // Place Details API 사용을 위해 Place ID가 필요합니다.
            // 현재 Place 엔티티에 Place ID가 없다면, Nearby Search로 Place ID를 먼저 가져와야 합니다.
            String query = place.getTitle() + " " + place.getAddr1();
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());

            // Nearby Search API를 통해 Place ID 가져오기
            String nearbySearchUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?query="
                    + encodedQuery + "&key=" + apiKey;

            String nearbyResponse = restTemplate.getForObject(nearbySearchUrl, String.class);
            JsonNode nearbyRoot = objectMapper.readTree(nearbyResponse);
            JsonNode nearbyResults = nearbyRoot.path("results");

            if (nearbyResults.isArray() && nearbyResults.size() > 0) {
                String placeId = nearbyResults.get(0).path("place_id").asText(null);
                if (placeId != null) {
                    // Place Details API를 통해 상세 정보 가져오기
                    String detailsUrl = "https://maps.googleapis.com/maps/api/place/details/json?place_id="
                            + placeId + "&fields=rating,user_ratings_total,price_level,opening_hours,website,photos&key=" + apiKey;

                    String detailsResponse = restTemplate.getForObject(detailsUrl, String.class);
                    JsonNode detailsRoot = objectMapper.readTree(detailsResponse);
                    JsonNode detailsResult = detailsRoot.path("result");

                    if (!detailsResult.isMissingNode()) {
                        // 평점 및 평점 수
                        double rating = detailsResult.path("rating").asDouble(0.0);
                        int userRatingsTotal = detailsResult.path("user_ratings_total").asInt(0);
                        place.updateRating(rating);
                        place.updateRatingTotal(userRatingsTotal);

                        // 가격 수준
                        int priceLevel = detailsResult.path("price_level").asInt(0);
                        place.updatePriceLevel(priceLevel);

                        // 오픈 시간
                        JsonNode openingHours = detailsResult.path("opening_hours");
                        if (!openingHours.isMissingNode()) {
                            String hours = openingHours.toString(); // JSON 형태로 저장
                            place.updateOpeningHours(hours);
                        }

                        // 웹사이트
                        String website = detailsResult.path("website").asText(null);
                        place.updateWebsite(website);

                        // 사진 참조 ID
                        JsonNode photos = detailsResult.path("photos");
                        if (photos.isArray() && photos.size() > 0) {
                            String photoReference = photos.get(0).path("photo_reference").asText(null);
                            place.updatePhotoReference(photoReference);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return place;
    }

    @Override
    @Async
    @CachePut(value = "placeRatings", key = "#place.id")
    public CompletableFuture<Place> enrichPlaceWithDetailsAsync(Place place) {
        Place enrichedPlace = enrichPlaceWithDetails(place);
        return CompletableFuture.completedFuture(enrichedPlace);
    }

    @Override
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