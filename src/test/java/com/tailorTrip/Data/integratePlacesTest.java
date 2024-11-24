package com.tailorTrip.Data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tailorTrip.Repository.PlaceRepository;
import com.tailorTrip.domain.Place;
import com.tailorTrip.service.KorService;
import com.tailorTrip.service.PlaceService;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@SpringBootTest
public class integratePlacesTest {

    @Autowired
    private PlaceService placeService; // PlaceService

    @Autowired
    private PlaceRepository placeRepository; // PlaceRepository (DB 작업을 위한 리포지토리)

    @Getter
    @Setter
    static class NewPlace {
        private String baseYm;
        private double mapX;
        private double mapY;
        private String areaCd;
        private String areaNm;
        private String signguCd;
        private String signguNm;
        private String hubTatsNm;
        private String hubBsicAdres;
        private String hubCtgryLclsNm;
        private String hubCtgryMclsNm;
        private String hubRank;

        // 생성자, getter, setter, toString 생략
    }

    @Test
    void integratePlacesTest() throws IOException {
        // 1. 기존 데이터와 새 데이터 로드
        List<Place> existingPlaces = placeRepository.findAll(); // 기존 데이터 (메모리에서 가져온다고 가정)
        List<NewPlace> newPlaces = loadNewPlaces();       // 새 데이터 (JSON에서 읽어옴)

        // 2. 새 데이터를 기존 데이터에 통합
        for (NewPlace newPlace : newPlaces) {

            for (Place existingPlace : existingPlaces) {
                double similarity = calculateSimilarity(existingPlace, newPlace);
                if (similarity >= 0.9) { // 유사도가 85% 이상일 때 통합
                    placeService.updatePlaceHubRank(existingPlace, newPlace.getHubRank());
                    break;
                }
            }

        }

        // 3. 통합 결과 출력
        existingPlaces.forEach(System.out::println); // 결과 확인
    }


    // 새 데이터 로드 (JSON 파일 읽기)
    private List<NewPlace> loadNewPlaces() throws IOException {
        String jsonData = new String(Files.readAllBytes(Paths.get("src/main/resources/tourist_data_combined.json")));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonData, new TypeReference<List<NewPlace>>() {});
    }

    private double calculateSimilarity(Place place, NewPlace newPlace) {
        double coordinateSimilarity = calculateCoordinateSimilarity(place.getMapX(), place.getMapY(), newPlace.getMapX(), newPlace.getMapY());
        double titleSimilarity = calculateTitleSimilarity(place.getTitle(), newPlace.getHubTatsNm());
        double addressSimilarity = calculateAddressSimilarity(place.getAddr1(), newPlace.getHubBsicAdres());

        // 가중치 적용
        return 0.4 * coordinateSimilarity + 0.3 * titleSimilarity + 0.3 * addressSimilarity;
    }


    // 좌표 비교 (거리 계산)
    private double calculateCoordinateSimilarity(double mapX1, double mapY1, double mapX2, double mapY2) {
        double distance = Math.sqrt(Math.pow(mapX1 - mapX2, 2) + Math.pow(mapY1 - mapY2, 2));
        double threshold = 0.001; // 약 111m
        return distance <= threshold ? 1.0 : 0.0;
    }

    // 이름 비교 (문자열 유사도 - Levenshtein Distance)
    private double calculateTitleSimilarity(String title1, String title2) {
        int maxLength = Math.max(title1.length(), title2.length());
        int distance = levenshteinDistance(title1, title2);
        return 1.0 - ((double) distance / maxLength);
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    );
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    private double calculateAddressSimilarity(String address1, String address2) {
        return calculateTitleSimilarity(address1, address2); // 주소도 유사도 계산 적용
    }


}
