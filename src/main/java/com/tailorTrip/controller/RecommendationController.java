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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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



    @GetMapping("/home")
    public String showHomePage() {
        return "home/home"; // home.html이 static 폴더 아래에 있음
    }


    @PostMapping("/submit-preferences")
    public Map<String, String > submitAllPreferences(@RequestBody UserPreferencesDTO prefsDTO, Model model) {

        // 지역에 따른 중심 좌표 가져오기
        double[] coordinates = regionService.getCoordinates(prefsDTO.getRegion());
        double userLat = coordinates[0];
        double userLng = coordinates[1];

        // UserPreferences 객체 생성
        UserPreferences prefs = UserPreferences.builder()
                .region(prefsDTO.getRegion())
                .tripDuration(prefsDTO.getTripDuration())
                .interest(prefsDTO.getInterests().get(0))
                .activityType(String.join(prefsDTO.getActivities().get(0)))
                .petFriendly(prefsDTO.isPetFriendly())
                .foodPreference(prefsDTO.getFoodPreferences().get(0))
                .travelPace(prefsDTO.getTravelPace())
                .accommodationPreference(prefsDTO.getAccommodationPreference())
                .userLat(userLat)
                .userLng(userLng)
                .build();

        // 사용자 선호도 저장
        userPreferencesRepository.save(prefs);

        // 일정 생성
        Itinerary itinerary = itineraryService.createItinerary(prefs);

        // 모델에 일정 추가
        model.addAttribute("itinerary", itinerary);
        model.addAttribute("message", "선호도가 성공적으로 저장되었고, 일정이 생성되었습니다."); // 메시지도 모델에 추가

        Map<String, String> map = new HashMap<>();
        map.put("success", prefsDTO.getRegion());

        return map;
    }
}
