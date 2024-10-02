package com.tailorTrip.service;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.ZeroResultsException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.TravelMode;
import com.tailorTrip.domain.Place;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class DirectionsService {

    @Value("${google.api.key}")
    private String apikey;

    private GeoApiContext getGeoContext() {
        return new GeoApiContext.Builder()
                .apiKey(apikey)
                .build();
    }


    public List<Place> optimizeRoute(List<Place> places, String transportation) {
        if (places.size() < 2) {
            log.info("Not enough places to optimize route.");
            return Collections.emptyList(); // 빈 리스트 반환
        }

        GeoApiContext context = getGeoContext();

        // 출발지와 도착지 설정
        Place origin = places.get(0);
        Place destination = places.get(places.size() - 1);
        List<Place> waypoints = places.subList(1, places.size() - 1);

        try {
            DirectionsResult result = DirectionsApi.newRequest(context)
                    .mode(transportation.equalsIgnoreCase("걸어서") ? TravelMode.WALKING : TravelMode.DRIVING)
                    .origin(new com.google.maps.model.LatLng(origin.getLat(), origin.getLng()))
                    .destination(new com.google.maps.model.LatLng(destination.getLat(), destination.getLng()))
                    .waypoints(waypoints.stream()
                            .map(place -> new com.google.maps.model.LatLng(place.getLat(), place.getLng()))
                            .toArray(com.google.maps.model.LatLng[]::new))
                    .optimizeWaypoints(true)
                    .await();

            log.info("Directions API response: {} routes found.", result.routes.length);

            // 최적화된 순서를 기반으로 장소 재정렬
            int[] order = result.routes[0].waypointOrder;
            List<Place> optimizedPlaces = new ArrayList<>();
            optimizedPlaces.add(origin);

            for (int i = 0; i < order.length; i++) {
                optimizedPlaces.add(waypoints.get(order[i]));
            }

            optimizedPlaces.add(destination);

            log.info("Optimized route size: {}", optimizedPlaces.size());

            return optimizedPlaces;

        } catch (ZeroResultsException e) {
            // 특정 요청에 대해 경로를 찾을 수 없을 때 처리
            System.err.println("No route found for the given origin, destination, and waypoints.");
            e.printStackTrace();
            return places; // 최적화 실패 시 원래 리스트 반환
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
            return places; // 기타 예외 처리
        }
    }



//    public PlacesSearchResponse searchPlaces(String query, String lat, String lng) throws Exception {
//        LatLng location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
//        return PlacesApi.textSearchQuery(context, query)
//                .location(location)
//                .radius(1000) // 1km 범위 내 검색
//                .await();
//    }
}
