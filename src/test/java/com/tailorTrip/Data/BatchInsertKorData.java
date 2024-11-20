package com.tailorTrip.Data;

import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.Place;
import com.tailorTrip.service.KorService;
import com.tailorTrip.service.PlaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
@EnableAsync
@RequiredArgsConstructor
public class BatchInsertKorData {

    @Autowired
    private KorService korService; // 실제 서비스 클래스

    @Autowired
    private PlaceService placeService; // PlaceService

    @Autowired
    private PlaceRepository placeRepository; // PlaceRepository (DB 작업을 위한 리포지토리)


    @Value("${batch.size}") // 배치 크기를 외부 설정으로 주기
    private int batchSize;

    /**
     * 배치 처리하여 여러 Place를 동시에 처리하도록 수정
     */
    @Async
    public CompletableFuture<Void> processBatch(List<Place> places) throws InterruptedException {
        for (Place place : places) {
            // overview가 비어있는 경우에만 요청
            if (place.getOverview() == null || place.getOverview().isEmpty()) {
                try {
                    // getOverview를 호출하여 overview 가져오기
                    String overview = korService.getOverview(place.getContentId(), place.getContentTypeId());
                    // PlaceService를 통해 DB에 업데이트
                    placeService.updatePlaceOverview(place, overview);
                } catch (Exception e) {
                    System.out.println("Error processing place " + place.getContentId() + ": " + e.getMessage());
                }
            }

            // 이미지가 비어있는 경우에만 요청
            if (place.getFirstImage() == null || place.getFirstImage().isEmpty()) {
                try {
                    String imageUrl = korService.getImages(place.getContentId());
                    placeService.updatePlaceImage(place, imageUrl);
                } catch (Exception e) {
                    System.out.println("Error processing image for place " + place.getContentId() + ": " + e.getMessage());
                }
            }

            // 요청 간의 지연 (배치 크기마다 1초 지연)
            if (places.indexOf(place) % batchSize == 0 && places.indexOf(place) > 0) {
                Thread.sleep(1000); // 배치 크기마다 지연
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    @Test
    public void testGetOverviewAndInsertIntoDB() throws InterruptedException {
        // DB에서 모든 Place를 가져옵니다.
        List<Place> places = placeRepository.findAll();

        // 전체 데이터를 배치 크기만큼 나눠서 처리
        for (int i = 0; i < places.size(); i += batchSize) {
            int end = Math.min(i + batchSize, places.size());
            List<Place> batch = places.subList(i, end);

            // 비동기 처리 (배치별로 처리)
            processBatch(batch);
        }

        // CompletableFuture 기다리기 (배치 작업이 모두 끝날 때까지)
        CompletableFuture.allOf(processBatch(places)).join();
    }
}
