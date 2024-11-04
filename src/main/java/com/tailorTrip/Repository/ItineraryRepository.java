package com.tailorTrip.Repository;

import com.tailorTrip.domain.Itinerary;
import com.tailorTrip.dto.ItineraryDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

    Optional<Itinerary> findByIdAndMemberMid(Long id, String userId);
}
