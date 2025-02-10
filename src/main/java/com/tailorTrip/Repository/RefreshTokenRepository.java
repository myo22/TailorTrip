package com.tailorTrip.Repository;

import com.tailorTrip.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    void deleteByMid(String mid);
}
