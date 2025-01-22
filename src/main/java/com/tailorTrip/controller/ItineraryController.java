package com.tailorTrip.controller;

import com.tailorTrip.domain.Itinerary;
import com.tailorTrip.dto.ItineraryDTO;
import com.tailorTrip.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Log4j2
public class ItineraryController {

    private final ItineraryService itineraryService;

    @PostMapping("/save")
    public ResponseEntity<String> saveItinerary(@RequestBody ItineraryDTO itineraryDTO, Principal principal) {

        if(principal == null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("로그인이 필요합니다.");
        }

        String userId = principal.getName(); // 현재 로그인한 사용자 ID

        itineraryService.saveItinerary(itineraryDTO, userId);

        return ResponseEntity.ok("Itinerary saved successfully!");
    }

    // 일정 수정 API
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateItinerary(@PathVariable Long id, @RequestBody ItineraryDTO itineraryDTO, Principal principal) {
        String userId = principal.getName();
        itineraryService.updateItinerary(userId, id, itineraryDTO);
        return ResponseEntity.ok("Itinerary updated successfully!");
    }

    // 일정 불러오기 API
    @GetMapping("/{id}")
    public ResponseEntity<ItineraryDTO> getItinerary(@PathVariable Long id) {
        ItineraryDTO itineraryDTO = itineraryService.getItineraryById(id);
        return ResponseEntity.ok(itineraryDTO);
    }
}
