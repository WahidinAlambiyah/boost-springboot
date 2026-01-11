package com.example.boost;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BoostApplication {
    public static void main(String[] args) {
        SpringApplication.run(BoostApplication.class, args);
    }
}
