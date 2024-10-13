package com.tailorTrip.initializer;

import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.Place;
import com.tailorTrip.service.GooglePlacesService;
import com.tailorTrip.service.PlaceRatingUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final PlaceRatingUpdater placeRatingUpdater;

    @Override
    public void run(String... args) throws Exception {
        placeRatingUpdater.updatePlaceDetails();
        System.out.println("장소 상세 정보 초기화 완료.");
    }

    // 매일 새벽 2시에 업데이트 실행 (크론 표현식)
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledUpdate() {
        placeRatingUpdater.updatePlaceDetails();
        System.out.println("장소 상세 정보 주기적 업데이트 완료.");
    }
}
