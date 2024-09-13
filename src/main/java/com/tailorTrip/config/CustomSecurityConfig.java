package com.tailorTrip.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Log4j2
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class CustomSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        log.info("------------------configure----------------------");

        http.formLogin();

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        // 종류중에서 가장 무난, 해시 알고리즘으로 암호화 처리
        return new BCryptPasswordEncoder();
    }

    // css 파일, js 파일 등 정적파일들에는 굳이 시큐리티를 적용할 필요가 없다.
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){

        log.info("------------------web Configure-----------------------");

        return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }



}
