package com.tailorTrip.controller;

import com.tailorTrip.Repository.UserPreferencesRepository;
import com.tailorTrip.dto.Itinerary;
import com.tailorTrip.domain.UserPreferences;
import com.tailorTrip.dto.UserPreferencesDTO;
import com.tailorTrip.service.ItineraryServiceImpl;
import com.tailorTrip.service.RegionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
    public ResponseEntity<Itinerary> submitAllPreferences(@RequestBody UserPreferencesDTO prefsDTO) {

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
        Itinerary itinerary = itineraryService.createItinerary(prefs);

        return ResponseEntity.ok(itinerary); // 또는 다른 응답 객체를 반환
    }
}
