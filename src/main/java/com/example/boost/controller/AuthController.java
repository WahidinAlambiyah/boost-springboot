package com.example.boost.controller;

import com.example.boost.domain.dto.ApiResponse;
import com.example.boost.domain.dto.AuthResponse;
import com.example.boost.domain.dto.LoginRequest;
import com.example.boost.domain.dto.LogoutRequest;
import com.example.boost.domain.dto.RefreshRequest;
import com.example.boost.domain.dto.RegisterRequest;
import com.example.boost.domain.dto.RegisterResponse;
import com.example.boost.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "1. Authentication", description = "Login, logout, refresh token")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register user",
            description = "Register user baru"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Register berhasil",
                    useReturnTypeSchema = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "registerSuccess",
                                    value = """
                                        {
                                          "status": 201,
                                          "message": "User registered successfully",
                                          "data": {
                                            "username": "admin",
                                            "email": "legio@example.com",
                                            "roleCodes": ["ADMIN"]
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = "Validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "status": 422,
                                          "message": "Validation failed",
                                          "data": {
                                            "errors": {
                                              "email": "must be a well-formed email address"
                                            }
                                          }
                                        }
                                        """
                            )
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Register request",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(value = """
                                {
                                  "username": "admin",
                                  "email": "legio@example.com",
                                  "password": "123456789",
                                  "roleCodes": ["ADMIN"]
                                }
                            """)
                    )
            )
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "User registered successfully", response));
    }

    @Operation(
            summary = "Login user",
            description = "Authenticate user dan mengembalikan access token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login berhasil",
                    useReturnTypeSchema = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "loginSuccess",
                                    value = """
                                        {
                                          "status": 200,
                                          "message": "Logged in",
                                          "data": {
                                            "accessToken": "xxx",
                                            "refreshToken": "xxx",
                                            "tokenType": "Bearer"
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "unauthorized",
                                    value = """
                                        {
                                          "status": 401,
                                          "message": "Invalid credentials",
                                          "data": null
                                        }
                                        """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Credential user",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(value = """
                        {
                          "username": "user",
                          "password": "password123"
                        }
                    """)
                )
            )
        @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Logged in", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Token refreshed", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) LogoutRequest request) {
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7).trim();
            if (accessToken.isBlank()) {
                accessToken = null;
            }
        }
        String refreshToken = request != null ? request.getRefreshToken() : null;
        authService.logout(accessToken, refreshToken);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(
            path = {"/login", "/register", "/refresh", "/logout"},
            method = RequestMethod.HEAD)
    public ResponseEntity<Void> authHeadCheck() {
        return ResponseEntity.noContent().build();
    }
}
