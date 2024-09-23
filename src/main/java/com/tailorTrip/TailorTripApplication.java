package com.tailorTrip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TailorTripApplication {

    public static void main(String[] args) {
        SpringApplication.run(TailorTripApplication.class, args);
    }

}
