package com.tailorTrip.Repository;

import com.tailorTrip.domain.Itinerary;
import com.tailorTrip.dto.ItineraryDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

    Itinerary findByIdAndMid(Long id, String userId);
}
