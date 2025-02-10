package com.tailorTrip.security.filter;

import com.google.gson.Gson;
import com.tailorTrip.Repository.RefreshTokenRepository;
import com.tailorTrip.domain.RefreshToken;
import com.tailorTrip.security.exception.RefreshTokenException;
import com.tailorTrip.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
public class RefreshTokenFilter extends OncePerRequestFilter {

    private final String refreshPath;

    private final JWTUtil jwtUtil;

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!path.equals(refreshPath)){
            log.info("skip refresh token filter......");
            // 필터 체인을 계속 실행합니다. 다음 순서로 넘겨버림.
            filterChain.doFilter(request,response);
            return;
        }

        log.info("Refresh Token Filter...run............1");

        //전송된 JSON에서 accessToken과 refreshToken을 얻어온다.
        Map<String, String> tokens = parseRequestJSON(request);
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        log.info("accessToken: " + accessToken);
        log.info("refreshToken: " + refreshToken);

        try {
            checkAccessToken(accessToken);
        }catch (RefreshTokenException refreshTokenException){
            refreshTokenException.sendResponseError(response);
        }

        Map<String, Object> refreshClaims = null;

        try {

            refreshClaims = checkRefreshToken(refreshToken);
            log.info(refreshClaims);

            //Refresh Token의 유효시간이 얼마 남지 않은 경우
            Integer exp = (Integer)refreshClaims.get("exp");

            Date expTime = new Date(Instant.ofEpochMilli(exp).toEpochMilli() * 1000);

            Date current = new Date(System.currentTimeMillis());

            //만료 시간과 현재 시간의 간격 계산
            //만일 3일 미만인 경우에는 Refresh Token도 다시 생성
            long gapTime = (expTime.getTime() - current.getTime());

            log.info("-------------------------------------");
            log.info("current: " + current);
            log.info("expTime: " + expTime);
            log.info("gap: " + gapTime);

            String mid = (String)refreshClaims.get("mid");

            //이 상태까지 오면 무조건 AccessToken은 새로 생성
            String accessTokenValue = jwtUtil.generateToken(Map.of("mid", mid), 1);
            String refreshTokenValue = tokens.get("refreshToken");

            //RefreshToken이 3일도 안남았다면..
            if(gapTime < (1000 * 60 * 60 * 24 * 3 ) ){
                log.info("new Refresh Token required... ");
                refreshTokenValue = jwtUtil.generateToken(Map.of("mid", mid), 30);
            }

            // DB에서도 Refresh Token 업데이트
            refreshTokenRepository.save(new RefreshToken(null, mid, refreshTokenValue, LocalDateTime.now().plusDays(30)));

            log.info("Refresh Token result............................");
            log.info("accessToken: " + accessTokenValue);
            log.info("refreshToken: " + refreshTokenValue);

            sendTokens(accessTokenValue, refreshTokenValue, response);

        }catch (RefreshTokenException refreshTokenException){
            refreshTokenException.sendResponseError(response);
            return; // 더 이상 실행할 코드가 없음
        }
    }

    private Map<String, String> parseRequestJSON(HttpServletRequest request){

        //JSON 데이터를 분석해서 mid, mpw 전달 값을 Map으로 처리
        try(Reader reader = new InputStreamReader(request.getInputStream())){

            Gson gson = new Gson();
            // JSON 데이터를 Java의 Map<String, String> 형태로 변환합니다.
            return gson.fromJson(reader, Map.class);

        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
    }

    private void checkAccessToken(String accessToken) throws RefreshTokenException {
        try {
            jwtUtil.validateToken(accessToken);
        }catch (ExpiredJwtException exception){
            log.info("Access Token has expired");
        }catch (Exception exception){
            throw new RefreshTokenException(RefreshTokenException.ErrorCase.NO_ACCESS);
        }
    }

    private Map<String, Object> checkRefreshToken(String refreshToken) throws RefreshTokenException{

        try {
            Map<String, Object> values = jwtUtil.validateToken(refreshToken);

            String mid = (String) values.get("mid");

            Optional<RefreshToken> storedTokenOpt = refreshTokenRepository.findByMid(mid);

            if (storedTokenOpt.isEmpty() || !storedTokenOpt.get().getToken().equals(refreshToken)) {
                log.warn("Invalid Refresh Token: Token does not match DB record");
                throw new RefreshTokenException(RefreshTokenException.ErrorCase.NO_REFRESH);
            }

            return values;
        }catch(ExpiredJwtException expiredJwtException) {
            throw new RefreshTokenException(RefreshTokenException.ErrorCase.OLD_REFRESH);
        }catch (MalformedJwtException malformedJwtException){
            log.error("MalformedJwtException------------------");
            throw new RefreshTokenException(RefreshTokenException.ErrorCase.NO_REFRESH);
        }catch (Exception exception){
            new RefreshTokenException(RefreshTokenException.ErrorCase.NO_REFRESH);
        }
        return null;
    }

    private void sendTokens(String accessTokenValue, String refreshTokenValue, HttpServletResponse response){

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Gson gson = new Gson();

        String jsonStr = gson.toJson(Map.of("accessToken", accessTokenValue, "refreshToken", refreshTokenValue));

        try {
            response.getWriter().println(jsonStr);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
