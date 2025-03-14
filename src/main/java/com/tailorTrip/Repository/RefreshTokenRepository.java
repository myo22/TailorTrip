package com.tailorTrip.Repository;

import com.tailorTrip.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByMid(String mid);
    @Transactional
    void deleteByMid(String mid);
    void deleteByToken(String token);
}
