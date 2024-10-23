package com.tailorTrip.service;

import com.tailorTrip.domain.DetailInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KorServiceImpl implements KorService {

    private final RestTemplate restTemplate;
    private final String baseUrl = "http://apis.data.go.kr/B551011/KorService1/";

    @Value("${kor.api.key}")
    private String serviceKey;

    @Override
    // overview 가져오는 함수
    public String getOverview(Integer contentId, Integer contentTypeId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "detailCommon1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", contentTypeId)
                .queryParam("defaultYN", "Y")
                .queryParam("firstImageYN", "N")
                .queryParam("areacodeYN", "N")
                .queryParam("catcodeYN", "N")
                .queryParam("addrinfoYN", "N")
                .queryParam("mapinfoYN", "N")
                .queryParam("overviewYN", "Y")
                .queryParam("numOfRows", "1")
                .queryParam("pageNo", "1")
                .toUriString();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            Map<String, Object> body = (Map<String, Object>) response.get("response");
            Map<String, Object> items = (Map<String, Object>) ((Map<String, Object>) body.get("body")).get("items");
            Map<String, Object> item = (Map<String, Object>) ((Map<String, Object>) items).get("item");
            return (String) item.getOrDefault("overview", "No overview available");
        } catch (Exception e) {
            System.out.println("API 호출 오류: " + e.getMessage());
            return null;
        }
    }

    @Override
    // intro 정보 가져오는 함수
    public List<DetailInfo> getIntro(Integer contentId, Integer contentTypeId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "detailIntro1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", contentTypeId)
                .queryParam("numOfRows", "1")
                .queryParam("pageNo", "1")
                .toUriString();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            Map<String, Object> body = (Map<String, Object>) response.get("response");
            Map<String, Object> items = (Map<String, Object>) ((Map<String, Object>) body.get("body")).get("items");
            return (List<DetailInfo>) ((Map<String, Object>) items).get("item");
        } catch (Exception e) {
            System.out.println("API 호출 오류: " + e.getMessage());
            return null;
        }
    }

    @Override
    // 상세 정보(detailInfo) 가져오는 함수
    public Map<String, String> getDetailInfo(Integer contentId, Integer contentTypeId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "detailInfo1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "AppTest")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", contentTypeId)
                .queryParam("numOfRows", "10")
                .queryParam("pageNo", "1")
                .toUriString();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            Map<String, Object> body = (Map<String, Object>) response.get("response");
            Map<String, Object> items = (Map<String, Object>) ((Map<String, Object>) body.get("body")).get("items");
            return (Map<String, String>) ((Map<String, Object>) items).get("item");
        } catch (Exception e) {
            System.out.println("API 호출 오류: " + e.getMessage());
            return null;
        }
    }
}