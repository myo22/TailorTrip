package com.tailorTrip.config;

import com.tailorTrip.security.CustomUserDetailsService;
import com.tailorTrip.security.filter.APILoginFilter;
import com.tailorTrip.security.filter.RefreshTokenFilter;
import com.tailorTrip.security.filter.TokenCheckFilter;
import com.tailorTrip.security.handler.APILoginSuccessHandler;
import com.tailorTrip.security.handler.Custom403Handler;
import com.tailorTrip.security.handler.CustomSocialLoginSuccessHandler;
import com.tailorTrip.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@Log4j2
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class CustomSecurityConfig {

    //주입 필요
    private final DataSource dataSource;
    private final CustomUserDetailsService userDetailsService;
    private final JWTUtil jwtUtil;

    @Bean
    public PasswordEncoder passwordEncoder(){
        // 종류중에서 가장 무난, 해시 알고리즘으로 암호화 처리
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {

        // AuthenticationManager 설정
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());

        // Get AuthenticationManager
        AuthenticationManager authenticationManager =
                authenticationManagerBuilder.build();

        // 반드시 필요
        http.authenticationManager(authenticationManager);

        // APILoginFilter
        APILoginFilter apiLoginFilter = new APILoginFilter("/generateToken");
        apiLoginFilter.setAuthenticationManager(authenticationManager);

        // APILoginSuccessHandler
        APILoginSuccessHandler successHandler = new APILoginSuccessHandler(jwtUtil);
        // SuccessHandler 세팅
        apiLoginFilter.setAuthenticationSuccessHandler(successHandler);

        // APILoginFilter의 위치 조정
        http.addFilterBefore(apiLoginFilter, UsernamePasswordAuthenticationFilter.class);

        //api로 시작하는 모든 경로는 TokenCheckFilter 동작
        http.addFilterBefore(
                tokenCheckFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class
        );

        // refreshToken 호출처리
        http.addFilterBefore(new RefreshTokenFilter("/refreshToken", jwtUtil),
                TokenCheckFilter.class);

        // 로그인 화면에서 로그인을 진행한다는 설정, 커스텀 로그인 페이지
        http.formLogin(form -> form.loginPage("/member/login"));

        // CSRF 토큰 비활성화
        http.csrf(csrf -> csrf.disable());

        http.rememberMe(rememberMe -> rememberMe
                .key("12345678")
                .tokenRepository(persistentTokenRepository())
                .userDetailsService(userDetailsService)
                .tokenValiditySeconds(60*60*24*30));

        http.exceptionHandling(exceptionHandling -> exceptionHandling.accessDeniedHandler(accessDeniedHanler())); // 403

        http.oauth2Login(oauth2Login ->
                oauth2Login.loginPage("/member/login")
                        .defaultSuccessUrl("/") // 로그인 성공 후 리디렉션 URL
                        .failureUrl("/member/login?error=true") // 로그인 실패 시
                        .successHandler(authenticationSuccessHandler()));


        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHanler() {
        return new Custom403Handler();
    }

    // css 파일, js 파일 등 정적파일들에는 굳이 시큐리티를 적용할 필요가 없다.
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){

        log.info("------------------web Configure-----------------------");

        return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository(){
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
        return repo;
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(){
        return new CustomSocialLoginSuccessHandler(passwordEncoder());
    }

    private TokenCheckFilter tokenCheckFilter(JWTUtil jwtUtil){
        return new TokenCheckFilter(jwtUtil);
    }
}
