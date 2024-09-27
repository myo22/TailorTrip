package com.tailorTrip.Repository;

import com.tailorTrip.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Place, Long>
{
}
