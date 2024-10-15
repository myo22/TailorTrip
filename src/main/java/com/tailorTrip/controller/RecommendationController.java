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

    // 초기 사용자 선호도 DTO를 세션에 저장하는 메서드
    private UserPreferencesDTO getUserPreferencesDTO(HttpSession session) {
        UserPreferencesDTO dto = (UserPreferencesDTO) session.getAttribute("userPreferences");
        if (dto == null) {
            dto = new UserPreferencesDTO();
            session.setAttribute("userPreferences", dto);
        }
        return dto;
    }


    @GetMapping("/home")
    public String showHomePage() {
        return "home/home"; // home.html이 static 폴더 아래에 있음
    }

    @PostMapping("/submit-region")
    public ResponseEntity<Map<String, String>> submitRegion(@RequestBody Map<String, String> payload,  HttpSession session) {
        String region = payload.get("region");
        UserPreferencesDTO dto = getUserPreferencesDTO(session);
        dto.setRegion(region);

        // JSON 형식의 응답 생성
        Map<String, String> response = new HashMap<>();
        response.put("region", region); // 추가된 부분

        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit-dates")
    public ResponseEntity<String> submitDates(@RequestBody Map<String, String> payload,  HttpSession session) {
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
        UserPreferencesDTO dto = getUserPreferencesDTO(session);
        dto.setTripDuration((int) tripDuration);

        return ResponseEntity.ok("Dates received: " + startDateStr + " to " + endDateStr + ", Duration: " + tripDuration + " days");
    }

    @PostMapping("/submit-interests")
    public ResponseEntity<String> submitInterests(@RequestBody Map<String, Object> payload,  HttpSession session) {
        List<String> interests = (List<String>) payload.get("interests");
        UserPreferencesDTO dto = getUserPreferencesDTO(session);
        dto.setInterests(interests.get(0));

        return ResponseEntity.ok(interests.get(0));
    }

    @PostMapping("/submit-activities")
    public ResponseEntity<String> submitActivities(@RequestBody Map<String, Object> payload,  HttpSession session) {
        List<String> activities = (List<String>) payload.get("activities");
        String petPreferenceStr = (String) payload.get("petPreference");

        // 문자열을 boolean으로 변환
        boolean petPreference = "yes".equalsIgnoreCase(petPreferenceStr); // "yes"면 true, 아니면 false로 변환

        UserPreferencesDTO dto = getUserPreferencesDTO(session);
        dto.setActivities(activities.get(0));
        dto.setPetFriendly(petPreference);

        return ResponseEntity.ok("Activities received: " + activities + ", Pet Preference: " + petPreference);
    }

    @PostMapping("/submit-food-and-travel-style")
    public ResponseEntity<String> submitFoodAndTravelStyle(@RequestBody Map<String, Object> payload, HttpSession session) {
        List<String> foodPreferences = (List<String>) payload.get("foodPreferences");
        String travelStyle = (String) payload.get("travelStyle");

        UserPreferencesDTO dto = getUserPreferencesDTO(session);
        dto.setFoodPreferences(foodPreferences.get(0));
        dto.setTravelPace(travelStyle);
        return ResponseEntity.ok("Food Preferences received: " + foodPreferences + ", Travel Style: " + travelStyle);
    }

    @PostMapping("/submit-accommodation")
    public ResponseEntity<String> submitAccommodation(@RequestBody Map<String, String> payload, HttpSession session) {
        String accommodation = payload.get("accommodation");
        UserPreferencesDTO dto = getUserPreferencesDTO(session);
        dto.setAccommodationPreference(accommodation);
        return ResponseEntity.ok("Accommodation received: " + accommodation);
    }

    @PostMapping("/submit-all")
    public String submitAllPreferences(Model model, HttpSession session) {

        UserPreferencesDTO dto = getUserPreferencesDTO(session);

        // 지역에 따른 중심 좌표 가져오기
        double[] coordinates = regionService.getCoordinates(dto.getRegion());
        double userLat = coordinates[0];
        double userLng = coordinates[1];

        // UserPreferences 객체 생성
        UserPreferences prefs = UserPreferences.builder()
                .region(dto.getRegion())
                .tripDuration(dto.getTripDuration())
                .interest(String.join(", ", dto.getInterests()))
                .activityType(String.join(", ", dto.getActivities()))
                .petFriendly(dto.isPetFriendly())
                .foodPreference(String.join(", ", dto.getFoodPreferences()))
                .travelPace(dto.getTravelPace())
                .accommodationPreference(dto.getAccommodationPreference())
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
