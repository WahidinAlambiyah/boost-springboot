package com.example.boost.iam.api;

import com.example.boost.domain.dto.ApiResponse;
import com.example.boost.domain.dto.AuthResponse;
import com.example.boost.domain.dto.LoginRequest;
import com.example.boost.domain.dto.LogoutRequest;
import com.example.boost.domain.dto.RefreshRequest;
import com.example.boost.domain.dto.RegisterRequest;
import com.example.boost.domain.dto.RegisterResponse;
import com.example.boost.exception.ServiceUnavailableException;
import com.example.boost.exception.TooManyRequestsException;
import com.example.boost.iam.application.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
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

    //    TODO: need confirm if unused for register public
//    @PostMapping(path = "/register/mock", consumes = "application/json", produces = "application/json")
//    @Operation(
//            summary = "Register user (mock/debug)",
//            description = "Endpoint simulasi untuk validasi mapping error registration dengan header debug"
//    )
//    @ApiResponses(value = {
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "201",
//                    description = "Register berhasil",
//                    content = @Content(
//                            mediaType = "application/json",
//                            examples = @ExampleObject(value = """
//                                {
//                                  "status": 201,
//                                  "message": "User registered successfully",
//                                  "data": {
//                                    "username": "admin",
//                                    "email": "legio@example.com",
//                                    "roleCodes": ["ADMIN"]
//                                  }
//                                }
//                            """)
//                    )
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "403",
//                    description = "Tidak berhak assign ADMIN",
//                    content = @Content(
//                            mediaType = "application/json",
//                            examples = @ExampleObject(value = """
//                                {
//                                  "status": 403,
//                                  "message": "Forbidden",
//                                  "data": null
//                                }
//                            """)
//                    )
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "429",
//                    description = "Rate limit registration",
//                    content = @Content(
//                            mediaType = "application/json",
//                            examples = @ExampleObject(value = """
//                                {
//                                  "status": 429,
//                                  "message": "Too many registration attempts. Please try again later.",
//                                  "data": null
//                                }
//                            """)
//                    )
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "500",
//                    description = "Internal server error",
//                    content = @Content(
//                            mediaType = "application/json",
//                            examples = @ExampleObject(value = """
//                                {
//                                  "status": 500,
//                                  "message": "Unexpected error",
//                                  "data": null
//                                }
//                            """)
//                    )
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "503",
//                    description = "Service unavailable",
//                    content = @Content(
//                            mediaType = "application/json",
//                            examples = @ExampleObject(value = """
//                                {
//                                  "status": 503,
//                                  "message": "Registration service temporarily unavailable",
//                                  "data": null
//                                }
//                            """)
//                    )
//            )
//    })
//    public ResponseEntity<ApiResponse<RegisterResponse>> registerMock(
//            @RequestHeader(value = "X-Debug-Case", required = false) String debugCase,
//            @RequestHeader(value = "X-Actor-Role", required = false) String actorRole,
//            @Valid @RequestBody RegisterRequest request) {
//        if ("rate-limit".equalsIgnoreCase(debugCase)) {
//            throw new TooManyRequestsException("Too many registration attempts");
//        }
//        if ("downstream-down".equalsIgnoreCase(debugCase)) {
//            throw new ServiceUnavailableException("Registration service temporarily unavailable");
//        }
//        if ("boom".equalsIgnoreCase(debugCase)) {
//            throw new RuntimeException("Unexpected registration error");
//        }
//        if (request.getRoleCodes() != null
//                && request.getRoleCodes().contains("ADMIN")
//                && !StringUtils.hasText(actorRole)) {
//            throw new AccessDeniedException("Forbidden");
//        }
//        if (request.getRoleCodes() != null
//                && request.getRoleCodes().contains("ADMIN")
//                && !"ADMIN".equalsIgnoreCase(actorRole)) {
//            throw new AccessDeniedException("Forbidden");
//        }
//
//        RegisterResponse response = authService.register(request);
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ApiResponse.success(HttpStatus.CREATED.value(), "User registered successfully", response));
//    }

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

    @Operation(
            summary = "Refresh access token",
            description = "Generate access token baru menggunakan refresh token yang valid"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Refresh berhasil",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "status": 200,
                                  "message": "Token refreshed",
                                  "data": {
                                    "accessToken": "xxx",
                                    "refreshToken": "xxx",
                                    "tokenType": "Bearer"
                                  }
                                }
                            """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh token tidak valid",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "status": 401,
                                  "message": "Invalid refresh token",
                                  "data": null
                                }
                            """)
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Token refreshed", response));
    }

    @Operation(
            summary = "Logout user",
            description = "Logout access/refresh token. Endpoint ini mengembalikan 204 No Content saat sukses."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Logout berhasil"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token tidak valid",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "status": 401,
                                  "message": "Invalid token",
                                  "data": null
                                }
                            """)
                    )
            )
    })
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
