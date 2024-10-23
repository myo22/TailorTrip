package com.tailorTrip.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tailorTrip.domain.Place;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class GooglePlacesServiceImpl implements GooglePlacesService {

    @Value("${google.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = "placeDetails", key = "#place.id")
    public Place enrichPlaceWithDetails(Place place) {
        log.info("enrichPlaceWithDetails called for Place ID: {}", place.getId()); // 로그 추가

        try {
            // 위도와 경도
            double latitude = place.getMapY();
            double longitude = place.getMapX();

            log.info("Fetching nearby places for latitude: {}, longitude: {}", latitude, longitude); // 로그 추가

            // Nearby Search API를 통해 Place ID 가져오기
            String nearbySearchUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                    + latitude + "," + longitude + "&radius=1500&key=" + apiKey;

            String nearbyResponse = restTemplate.getForObject(nearbySearchUrl, String.class);

            log.info("Nearby search response: {}", nearbyResponse); // 응답 로그 추가

            JsonNode nearbyRoot = objectMapper.readTree(nearbyResponse);
            JsonNode nearbyResults = nearbyRoot.path("results");

            if (nearbyResults.isArray() && nearbyResults.size() > 0) {
                String placeId = nearbyResults.get(0).path("place_id").asText(null);
                log.info("Found Place ID: {}", placeId); // 로그 추가

                if (placeId != null) {
                    // Place Details API를 통해 상세 정보 가져오기
                    String detailsUrl = "https://maps.googleapis.com/maps/api/place/details/json?place_id="
                            + placeId + "&fields=rating,user_ratings_total,price_level,opening_hours,website,photos&key=" + apiKey;

                    String detailsResponse = restTemplate.getForObject(detailsUrl, String.class);
                    log.info("Details response: {}", detailsResponse); // 응답 로그 추가

                    JsonNode detailsRoot = objectMapper.readTree(detailsResponse);
                    JsonNode detailsResult = detailsRoot.path("result");

//                    if (!detailsResult.isMissingNode()) {
//                        // 평점 및 평점 수
//                        double rating = detailsResult.path("rating").asDouble(0.0);
//                        int userRatingsTotal = detailsResult.path("user_ratings_total").asInt(0);
//                        place.updateRating(rating);
//                        place.updateRatingTotal(userRatingsTotal);
//
//                        // 가격 수준
//                        int priceLevel = detailsResult.path("price_level").asInt(0);
//                        place.updatePriceLevel(priceLevel);
//
//                        // 오픈 시간
//                        JsonNode openingHours = detailsResult.path("opening_hours");
//                        if (!openingHours.isMissingNode()) {
//                            String hours = openingHours.toString(); // JSON 형태로 저장
//                            place.updateOpeningHours(hours);
//                        }
//
//                        // 웹사이트
//                        String website = detailsResult.path("website").asText(null);
//                        place.updateWebsite(website);
//
//                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return place;
    }

    @Override
    @Async
    @CachePut(value = "placeDetails", key = "#place.id")
    public CompletableFuture<Place> enrichPlaceWithDetailsAsync(Place place) {
        Place enrichedPlace = enrichPlaceWithDetails(place);
        return CompletableFuture.completedFuture(enrichedPlace);
    }
}