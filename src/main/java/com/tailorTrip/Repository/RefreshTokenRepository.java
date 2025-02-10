package com.tailorTrip.Repository;

import com.tailorTrip.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByMid(String mid);
    void deleteByMid(String mid);
    void deleteByRefreshToken(String refreshToken);
}
