package com.tailorTrip.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 일정 이름 (예: "서울 1일 일정")

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member; // 일정을 생성한 사용자

    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItineraryItem> items; // 일정 항목들
}
