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
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KorServiceImpl implements KorService {

    private final RestTemplate restTemplate;

    private final Gson gson;

    @Value("${kor.api.key}")
    private String serviceKey;

    @Override
    // API에서 overview를 가져오는 함수 (detailCommon1 엔드포인트 사용)
    public String getOverview(int contentId, int contentTypeId) {
        String urlString = "http://apis.data.go.kr/B551011/KorService1/detailCommon1?serviceKey=" + serviceKey +
                "&MobileOS=ETC&MobileApp=AppTest&_type=json&contentId=" + contentId + "&contentTypeId=" + contentTypeId +
                "&defaultYN=Y&firstImageYN=N&areacodeYN=N&catcodeYN=N&addrinfoYN=N&mapinfoYN=N&overviewYN=Y&numOfRows=1&pageNo=1";

        StringBuilder overview = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                overview.append(inputLine);
            }
            in.close();

            JsonObject response = JsonParser.parseString(overview.toString()).getAsJsonObject();
            return response.getAsJsonObject("response").getAsJsonObject("body")
                    .getAsJsonObject("items").getAsJsonArray("item").get(0).getAsJsonObject().get("overview").getAsString();

        } catch (Exception e) {
            System.out.println("API 호출 오류: " + e.getMessage());
            return null;
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