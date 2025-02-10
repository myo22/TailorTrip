package com.tailorTrip.controller;

import com.tailorTrip.Repository.RefreshTokenRepository;
import com.tailorTrip.domain.Member;
import com.tailorTrip.dto.MemberJoinDTO;
import com.tailorTrip.dto.UserProfileDTO;
import com.tailorTrip.security.dto.MemberSecurityDTO;
import com.tailorTrip.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/member")
@Log4j2
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final RefreshTokenRepository refreshTokenRepository;

    @GetMapping("/login")
    public void loginGET(String error, String logout){
        log.info("login get..................");
        log.info("logout: "+ logout);

        if(logout != null){
            log.info("user logout.......");
        }
    }

    @GetMapping("/user")
    public ResponseEntity<UserProfileDTO> getCurrentUser(HttpServletRequest request, Principal principal){
        String authHeader = request.getHeader("Authorization");
        log.info("Authorization 헤더: " + authHeader);
        log.info("Principal 값: " + principal);

        Authentication authentication = (Authentication) principal;
        Object principalObj = authentication.getPrincipal();

        if (!(principalObj instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 올바르지 않은 인증 정보
        }

        UserDetails userDetails = (UserDetails) principalObj;

        if (!(userDetails instanceof MemberSecurityDTO)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // 403 응답
        }

        MemberSecurityDTO memberDTO = (MemberSecurityDTO) userDetails;

        UserProfileDTO userProfileDTO = UserProfileDTO.builder()
                .email(memberDTO.getEmail())
                .username(memberDTO.getUsername())
                .del(memberDTO.isDel())
                .social(memberDTO.isSocial()).build();


         return ResponseEntity.ok().body(userProfileDTO);
    }

    @PostMapping
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        refreshTokenRepository.deleteByRefreshToken(refreshToken);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/join")
    public void joinGET(){

        log.info("join get...");
    }

    @PostMapping("/join")
    public String joinPOST(MemberJoinDTO memberJoinDTO, RedirectAttributes redirectAttributes){

        log.info("join post...");
        log.info(memberJoinDTO);

        try {
            memberService.join(memberJoinDTO);
        } catch (MemberService.MidExistException e) {

            redirectAttributes.addFlashAttribute("error", "mid");
            return "redirect:/member/join";
        }

        redirectAttributes.addFlashAttribute("result", "success");

        return "redirect:/member/login"; // 회원가입 후 로그인
    }
}
