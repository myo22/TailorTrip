package com.tailorTrip.initializer;

import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.Place;
import com.tailorTrip.service.GooglePlacesService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final GooglePlacesService googlePlacesService;

    private final PlaceRepository placeRepository;

    @Override
    public void run(String... args) throws Exception {

        // 기존 데이터 삭제 (선택 사항)
        placeRepository.deleteAll();

        // 예시: 서울의 카페, 맛집, 관광명소 데이터를 가져와 저장
        List<Place> cafes = googlePlacesService.fetchPlaces("Seoul", "cafe");
        List<Place> restaurants = googlePlacesService.fetchPlaces("Seoul", "restaurant");
        List<Place> attractions = googlePlacesService.fetchPlaces("Seoul", "tourist attraction");

        placeRepository.saveAll(cafes);
        placeRepository.saveAll(restaurants);
        placeRepository.saveAll(attractions);
    }
}
