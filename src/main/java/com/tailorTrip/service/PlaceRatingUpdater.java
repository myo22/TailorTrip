package com.tailorTrip.service;

import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceRatingUpdater {

    private final PlaceRepository placeRepository;
    private final GooglePlacesService googlePlacesService;

    public void updatePlaceDetails() {
        List<Place> places = placeRepository.findAll();

        for (Place place : places) {
            Place enricgedPlace = googlePlacesService.enrichPlaceWithDetails(place);
            placeRepository.save(enricgedPlace);
        }

        System.out.println("장소 상세 정보 업데이트 완료");
   }
}
