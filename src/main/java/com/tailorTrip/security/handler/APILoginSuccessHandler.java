package com.tailorTrip.security.handler;

import com.google.gson.Gson;
import com.tailorTrip.Repository.RefreshTokenRepository;
import com.tailorTrip.domain.RefreshToken;
import com.tailorTrip.security.dto.MemberSecurityDTO;
import com.tailorTrip.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class APILoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        log.info("Login Success Handler..............");

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);;

        log.info(authentication);
        log.info(authentication.getName()); // username

        MemberSecurityDTO memberSecurityDTO = (MemberSecurityDTO) authentication.getPrincipal();

        Map<String, Object> claim = Map.of("mid", authentication.getName());

        //Access Token 유효기간 1일
        String accessToken = jwtUtil.generateToken(claim, 1);
        // Refresh Token 유효기간 30일
        String refreshToken = jwtUtil.generateToken(claim, 30);

        // 기존에 있는 RefreshToken 삭제 후 저장 (중복 방지)
        refreshTokenRepository.deleteByMid(memberSecurityDTO.getEmail()); // 기존 토큰 제거
        refreshTokenRepository.save(new RefreshToken(null, memberSecurityDTO.getEmail(), refreshToken, LocalDateTime.now().plusDays(30)));

        Gson gson = new Gson();

        Map<String, String> keyMap = Map.of("accessToken", accessToken,
                "refreshToken", refreshToken);

        String jsonStr = gson.toJson(keyMap);

        response.getWriter().println(jsonStr);
    }
}
