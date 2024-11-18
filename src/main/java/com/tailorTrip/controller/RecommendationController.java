package com.tailorTrip.controller;

import com.tailorTrip.Repository.UserPreferencesRepository;
import com.tailorTrip.dto.ItineraryDTO;
import com.tailorTrip.domain.UserPreferences;
import com.tailorTrip.dto.ItineraryDay;
import com.tailorTrip.dto.ItineraryItem;
import com.tailorTrip.dto.UserPreferencesDTO;
import com.tailorTrip.service.ItineraryServiceImpl;
import com.tailorTrip.service.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
public class RecommendationController {

    private final UserPreferencesRepository userPreferencesRepository;

    private final ItineraryServiceImpl itineraryService;

    private final RegionService regionService;


    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home/home.html"; // static 폴더 아래의 home.html로 리디렉션
    }


    @PostMapping("/submit-preferences")
    public ResponseEntity<List<Map<String, Object>>> submitAllPreferences(@RequestBody UserPreferencesDTO prefsDTO) {

        // 지역에 따른 중심 좌표 가져오기
        double[] coordinates = regionService.getCoordinates(prefsDTO.getRegion());
        double userLat = coordinates[0];
        double userLng = coordinates[1];

        // UserPreferences 객체 생성
        UserPreferences prefs = UserPreferences.builder()
                .region(prefsDTO.getRegion())
                .tripDuration(prefsDTO.getTripDuration())
                .interest(prefsDTO.getInterests())
                .activityType(prefsDTO.getActivities())
                .petFriendly(prefsDTO.isPetPreference())
                .foodPreference(prefsDTO.getFoodPreferences())
                .travelPace(prefsDTO.getTravelStyle())
                .accommodationPreference(prefsDTO.getAccommodation())
                .userLat(userLat)
                .userLng(userLng)
                .build();

        // 사용자 선호도 저장
        userPreferencesRepository.save(prefs);

        // 일정 생성
        ItineraryDTO itinerary = itineraryService.createItinerary(prefs);

        // 프론트엔드 형식에 맞게 데이터를 변환
        List<Map<String, Object>> responseData = new ArrayList<>();
        for (ItineraryDay day : itinerary.getDays()) {
            for (ItineraryItem item : day.getItems()) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("contentid", item.getPlace().getContentId().toString());
                itemData.put("contenttypeid", String.valueOf(day.getDayNumber()));
                itemData.put("infoname", item.getTimeOfDay());
                itemData.put("infotext", item.getPlace().getOverview());
                itemData.put("label", item.getActivityType());
                itemData.put("name", item.getPlace().getTitle());
                itemData.put("lat", item.getPlace().getMapY());
                itemData.put("lng", item.getPlace().getMapX());
                itemData.put("url", item.getPlace().getFirstImage());
                itemData.put("address", item.getPlace().getAddr1());

                responseData.add(itemData);
            }
        }

        return ResponseEntity.ok(responseData);
    }
}
