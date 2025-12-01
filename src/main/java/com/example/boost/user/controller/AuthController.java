package com.example.boost.user.controller;

import com.example.boost.user.dto.LoginRequest;
import com.example.boost.user.dto.RegisterRequest;
import com.example.boost.user.model.User;
import com.example.boost.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public ResponseEntity<?> login(@Valid LoginRequest request, HttpServletResponse response) {
        return userService.authenticate(request)
                .map(user -> {
                    String token = userService.issueToken(user);
                    Cookie cookie = new Cookie("jwt", token);
                    cookie.setHttpOnly(true);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    return ResponseEntity.ok(Map.of(
                            "message", "Login success",
                            "token", token,
                            "role", user.getRole()
                    ));
                })
                .orElse(ResponseEntity.status(401).body(Map.of("message", "Invalid credentials")));
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }
}
