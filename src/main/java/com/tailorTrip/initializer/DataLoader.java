package com.tailorTrip.initializer;

import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.Place;
import com.tailorTrip.service.GooglePlaceService;
import com.tailorTrip.service.GooglePlacesService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final GooglePlacesService googlePlacesService;

    private PlaceRepository placeRepository;

    @Override
    public void run(String... args) throws Exception {
        // 카테고리별로 장소 데이터를 수집하고 저장
        String location = "Seoul"; // 예시 지역

        String[] categories = {"카페", "맛집", "관광명소", "산책"};

        for(String category : categories) {
            List<Place> places = googlePlacesService.fetchPlaces(location, category);
            placeRepository.saveAll(places);
        }
    }
}
