package com.tailorTrip.config;

import com.tailorTrip.security.CustomUserDetailsService;
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

    @Bean
    public PasswordEncoder passwordEncoder(){
        // 종류중에서 가장 무난, 해시 알고리즘으로 암호화 처리
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        log.info("------------------configure----------------------");

        // 커스텀 로그인 페이지
        http.formLogin(form -> form.loginPage("/member/login"));
        // CSRF 토큰 비활성화
        http.csrf(csrf -> csrf.disable()
        );

        http.rememberMe(rememberMe -> rememberMe
                .key("12345678")
                .tokenRepository(persistentTokenRepository())
                .userDetailsService(userDetailsService)
                .tokenValiditySeconds(60*60*24*30));

        return http.build();
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

}
