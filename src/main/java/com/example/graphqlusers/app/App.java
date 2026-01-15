package com.example.graphqlusers.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.graphqlusers")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
