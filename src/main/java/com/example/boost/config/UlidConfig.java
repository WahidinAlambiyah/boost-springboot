package com.example.boost.config;

import com.github.f4b6a3.ulid.UlidCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

@Configuration
public class UlidConfig {

    @Bean
    public Supplier<String> ulidSupplier() {
        return () -> UlidCreator.getUlid().toString();
    }
}
