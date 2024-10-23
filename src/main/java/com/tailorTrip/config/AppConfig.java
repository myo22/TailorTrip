package com.tailorTrip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // JSON 변환기 추가
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        // XML 응답 처리를 위한 JAXB 변환기
        restTemplate.getMessageConverters().add(new Jaxb2RootElementHttpMessageConverter());
        return restTemplate;
    }
}
