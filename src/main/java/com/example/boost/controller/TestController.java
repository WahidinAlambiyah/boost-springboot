package com.example.boost.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.boost.dto.RegisterUserRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public/test")
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterUserRequest request) {
        log.info("Received registration request for username: {}", request.getUsername());
        validatePassword();
        return ResponseEntity.ok().build();
    }

    private void validatePassword() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Konfirmasi password tidak sesuai.");
    }
    
}
