package com.tailorTrip.service;

import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.Place;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;

    @Override
    public List<Place> getRegionalPlaces(String region) {
        return placeRepository.findByAreaCodeEquals(region);
    }

    // Place 업데이트 메소드
    public void updatePlaceOverview(Place place, String overview) {
        place.updateOverview(overview);
        placeRepository.save(place); // 데이터베이스에 저장
    }
}
