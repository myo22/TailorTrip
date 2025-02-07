package com.tailorTrip.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String mid; // 이메일

    @Column(nullable = false, unique = true)
    private String token; // RefreshToken 값

    @Column(nullable = false)
    private LocalDateTime expiresAs; //만료시간

}
