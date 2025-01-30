package com.tailorTrip.controller;

import com.tailorTrip.domain.Member;
import com.tailorTrip.dto.MemberJoinDTO;
import com.tailorTrip.dto.UserProfileDTO;
import com.tailorTrip.security.dto.MemberSecurityDTO;
import com.tailorTrip.service.MemberService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/member")
@Log4j2
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/login")
    public void loginGET(String error, String logout){
        log.info("login get..................");
        log.info("logout: "+ logout);

        if(logout != null){
            log.info("user logout.......");
        }
    }

    @GetMapping("/user")
    public ResponseEntity<UserProfileDTO> getCurrentUser(Principal principal){
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();  // 인증되지 않은 경우 401 응답
        }

         Authentication authentication = (Authentication) principal;
         UserDetails userDetails = (UserDetails) authentication.getPrincipal();

         // UserDetails가 Member 엔티티와 연결되어 있기 때문에 가능
         Member member = (Member) userDetails;

         UserProfileDTO memberDTO = new UserProfileDTO(
                 member.getMid(),
                 member.getEmail(),
                 member.getRoleSet()
         );

         return ResponseEntity.ok().body(memberDTO);
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
