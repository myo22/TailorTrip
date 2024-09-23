package com.tailorTrip.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    private final String apiUrl = "https://api.openweathermap.org/data/2.5/weather";

    public String getWeather(String cityName) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("%s?q=%s&appid=%s&lang=kr&units=metric", apiUrl, cityName, apiKey);
        return restTemplate.getForObject(url, String.class);
    }
}
