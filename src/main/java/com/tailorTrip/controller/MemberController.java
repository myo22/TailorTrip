package com.tailorTrip.controller;

import com.tailorTrip.dto.MemberJoinDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
@Log4j2
@RequiredArgsConstructor
public class MemberController {

    @GetMapping("/login")
    public void loginGET(String error, String logout){
        log.info("login get..................");
        log.info("logout: "+ logout);

        if(logout != null){
            log.info("user logout.......");
        }
    }

    @GetMapping("/join")
    public void joinGET(){

        log.info("join get...");
    }

    @PostMapping("/join")
    public String joinPOST(MemberJoinDTO memberJoinDTO){

        log.info("join post...");
        log.info(memberJoinDTO);

        return "redirect:/";
    }
}
