package com.tailorTrip.Repository;

import com.tailorTrip.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    List<Place> findByAddr1Containing(String region);
}
