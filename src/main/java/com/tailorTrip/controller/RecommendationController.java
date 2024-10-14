package com.tailorTrip.controller;

import com.tailorTrip.Repository.UserPreferencesRepository;
import com.tailorTrip.dto.Itinerary;
import com.tailorTrip.domain.UserPreferences;
import com.tailorTrip.dto.UserPreferencesDTO;
import com.tailorTrip.service.ItineraryServiceImpl;
import com.tailorTrip.service.RegionService;
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
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
public class RecommendationController {

    private final UserPreferencesRepository userPreferencesRepository;

    private final ItineraryServiceImpl itineraryService;

    private final RegionService regionService;

    private UserPreferencesDTO userPreferencesDTO = new UserPreferencesDTO();

    @GetMapping("/home")
    public String showHomePage() {
        return "home/home"; // home.html이 static 폴더 아래에 있음
    }

    @PostMapping("/submit-region")
    public ResponseEntity<String> submitRegion(@RequestBody Map<String, String> payload) {
        String region = payload.get("region");
        System.out.println(region);
        userPreferencesDTO.setRegion(region);

        return ResponseEntity.ok("Region saved successfully");
    }

    @PostMapping("/submit-dates")
    public ResponseEntity<String> submitDates(@RequestBody Map<String, String> payload) {
        String startDateStr = payload.get("startDate");
        String endDateStr = payload.get("endDate");

        // 날짜 형식 정의 (예: "yyyy-MM-dd")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 문자열을 LocalDate로 변환
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);

        // 여행 기간 계산
        long tripDuration = ChronoUnit.DAYS.between(startDate, endDate);

        // DTO에 여행 기간 저장
        userPreferencesDTO.setTripDuration((int) tripDuration); // 필요한 경우 int로 변환

        return ResponseEntity.ok("Dates received: " + startDateStr + " to " + endDateStr + ", Duration: " + tripDuration + " days");
    }

    @PostMapping("/submit-interests")
    public ResponseEntity<String> submitInterests(@RequestBody List<String> interests) {
        userPreferencesDTO.setInterests(interests.get(0));
        return ResponseEntity.ok("Interests saved successfully");
    }

    @PostMapping("/submit-activities")
    public ResponseEntity<String> submitActivities(@RequestBody Map<String, Object> payload) {
        List<String> activities = (List<String>) payload.get("activities");
        String petPreferenceStr = (String) payload.get("petPreference");

        // 문자열을 boolean으로 변환
        boolean petPreference = "yes".equalsIgnoreCase(petPreferenceStr); // "yes"면 true, 아니면 false로 변환

        userPreferencesDTO.setActivities(activities.get(0));
        userPreferencesDTO.setPetFriendly(petPreference);
        return ResponseEntity.ok("Activities received: " + activities + ", Pet Preference: " + petPreference);
    }

    @PostMapping("/submit-food-and-travel-style")
    public ResponseEntity<String> submitFoodAndTravelStyle(@RequestBody Map<String, Object> payload) {
        List<String> foodPreferences = (List<String>) payload.get("foodPreferences");
        String travelStyle = (String) payload.get("travelStyle");
        userPreferencesDTO.setFoodPreferences(foodPreferences.get(0));
        userPreferencesDTO.setTravelPace(travelStyle);
        return ResponseEntity.ok("Food Preferences received: " + foodPreferences + ", Travel Style: " + travelStyle);
    }

    @PostMapping("/submit-accommodation")
    public ResponseEntity<String> submitAccommodation(@RequestBody Map<String, String> payload) {
        String accommodation = payload.get("accommodation");
        userPreferencesDTO.setAccommodationPreference(accommodation);
        return ResponseEntity.ok("Accommodation received: " + accommodation);
    }

    @PostMapping("/submit-all")
    public String submitAllPreferences(Model model) {

        // 지역에 따른 중심 좌표 가져오기
        double[] coordinates = regionService.getCoordinates(userPreferencesDTO.getRegion());
        double userLat = coordinates[0];
        double userLng = coordinates[1];

        UserPreferences prefs = UserPreferences.builder()
                .region(userPreferencesDTO.getRegion())
                .interest(userPreferencesDTO.getInterests())
                .activityType(userPreferencesDTO.getActivities())
                .petFriendly(userPreferencesDTO.isPetFriendly())
                .foodPreference(userPreferencesDTO.getFoodPreferences())
                .travelPace(userPreferencesDTO.getTravelPace())
                .accommodationPreference(userPreferencesDTO.getAccommodationPreference())
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

        // recommendResult 템플릿으로 포워딩
        return "recommendResult"; // templates/recommendResult.html로 포워딩
    }

    @GetMapping("/recommend")
    public String getRecommendationPage() {
        return "recommend";
    }


    @PostMapping("/recommend")
    public String recommend(
            @RequestParam String region,
            @RequestParam String interest,
            @RequestParam String activityType,
            @RequestParam boolean petFriendly,
            @RequestParam String foodPreference,
            @RequestParam String travelPace,
            @RequestParam String accommodationPreference,
            @RequestParam int tripDuration,
            Model model) {

        // 현재 로그인한 사용자 찾기
//        String username = authentication.getName();
//        Member member = memberRepository.findById(username).orElseThrow(() -> new RuntimeException("User not found"));


        // 지역에 따른 중심 좌표 가져오기
        double[] coordinates = regionService.getCoordinates(region);
        double userLat = coordinates[0];
        double userLng = coordinates[1];


        // 사용자 선호도 객체 생성
        UserPreferences prefs = UserPreferences.builder()
                .region(region)
                .interest(interest)
                .activityType(activityType)
                .petFriendly(petFriendly)
                .foodPreference(foodPreference)
                .travelPace(travelPace)
                .accommodationPreference(accommodationPreference)
                .tripDuration(tripDuration)
                .userLat(userLat)
                .userLng(userLng)
                .build();

        // 사용자 선호도 저장
        userPreferencesRepository.save(prefs);

        Itinerary itinerary = itineraryService.createItinerary(prefs);

        // 모델에 일정 추가
        model.addAttribute("itinerary", itinerary);

        return "recommendResult";
    }
}
