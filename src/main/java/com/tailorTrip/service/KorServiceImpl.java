package com.tailorTrip.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tailorTrip.domain.DetailInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KorServiceImpl implements KorService {

    private final RestTemplate restTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kor.api.key}")
    private String serviceKey;

    // API에서 overview를 가져오는 함수 (detailCommon1 엔드포인트 사용)
    public String getOverview(int contentId, int contentTypeId) {
        String urlString = String.format(
                "http://apis.data.go.kr/B551011/KorService1/detailCommon1?serviceKey=%s&MobileOS=ETC&MobileApp=AppTest&_type=json&contentId=%d&contentTypeId=%d&defaultYN=Y&firstImageYN=N&areacodeYN=N&catcodeYN=N&addrinfoYN=N&mapinfoYN=N&overviewYN=Y&numOfRows=1&pageNo=1",
                serviceKey, contentId, contentTypeId
        );

        StringBuilder overview = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // HTTP 응답 코드 체크
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("HTTP error code: " + responseCode);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                overview.append(inputLine);
            }
            in.close();

            // JSON 응답 파싱
            JsonNode response = objectMapper.readTree(overview.toString());
            String fullOverview = response.path("response").path("body").path("items").path("item").get(0).path("overview").asText("No overview available");

            // 글자수를 2000자로 제한
            if (fullOverview != null && !fullOverview.isEmpty()) {
                return fullOverview.length() > 2000 ? fullOverview.substring(0, 2000) : fullOverview; // 2000글자 초과 시 자르기
            }


            return "No overview available"; // overview가 비어있는 경우 처리
        } catch (Exception e) {
            System.out.println("overview API 호출 오류: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getImages(int contentId) {
        String urlString = String.format(
                "http://apis.data.go.kr/B551011/KorService1/detailImage1?serviceKey=%s&MobileOS=ETC&MobileApp=AppTest&_type=json&contentId=%d&imageYN=Y&subImageYN=Y&numOfRows=10&pageNo=1",
                serviceKey, contentId
        );

        StringBuilder responseBuilder = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // HTTP 응답 코드 확인
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("HTTP error code: " + responseCode);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                responseBuilder.append(inputLine);
            }
            in.close();

            // JSON 응답 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode response = objectMapper.readTree(responseBuilder.toString());
            JsonNode items = response.path("response").path("body").path("items").path("item");

            if (items.isArray() && items.size() > 0) {
                // 첫 번째 이미지 URL 가져오기
                return items.get(0).path("originimgurl").asText("No image available");
            }
            return "No image available";
        } catch (Exception e) {
            System.out.println("image API 호출 오류: " + e.getMessage());
            return "No image available";
        }
    }

    @Override
    // API에서 추가 정보를 가져오는 함수 (detailIntro1 엔드포인트 사용)
    public Map<String, Object> getIntro(Integer contentId, Integer contentTypeId) {
        String baseUrl = "http://apis.data.go.kr/B551011/KorService1/detailIntro1";
        String url = baseUrl + "?serviceKey=" + serviceKey + "&MobileOS=ETC&MobileApp=AppTest&_type=json&contentId=" + contentId + "&contentTypeId=" + contentTypeId + "&numOfRows=1&pageNo=1";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE); // Accept 헤더 설정

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> body = (Map<String, Object>) response.getBody().get("response");
            Map<String, Object> items = (Map<String, Object>) body.get("body");
            return (Map<String, Object>) ((List) items.get("items")).get(0);
        } catch (Exception e) {
            System.out.println("API 호출 오류: " + e.getMessage());
            return null;
        }
    }

    @Override
    // API에서 상세 정보를 가져오는 함수 (detailInfo1 엔드포인트 사용)
    public List<DetailInfo> getDetailInfo(Integer contentId, Integer contentTypeId) {
        String baseUrl = "http://apis.data.go.kr/B551011/KorService1/detailInfo1";
        String url = baseUrl + "?serviceKey=" + serviceKey + "&MobileOS=ETC&MobileApp=AppTest&_type=json&contentId=" + contentId + "&contentTypeId=" + contentTypeId + "&numOfRows=10&pageNo=1";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE); // Accept 헤더 설정

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> body = (Map<String, Object>) response.getBody().get("response");
            Map<String, Object> items = (Map<String, Object>) body.get("body");
            return (List<DetailInfo>) items.get("item");
        } catch (Exception e) {
            System.out.println("API 호출 오류: " + e.getMessage());
            return null;
        }
    }
}