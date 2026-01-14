package com.example.graphqlusers;

import com.example.graphqlusers.app.AppException;
import com.example.graphqlusers.app.AuthPayload;
import com.example.graphqlusers.app.ErrorCodes;
import com.example.graphqlusers.app.RegisterInput;
import com.example.graphqlusers.auth.AuthContext;
import com.example.graphqlusers.auth.JwtService;
import com.example.graphqlusers.repository.UserRepository;
import com.example.graphqlusers.service.AuthService;
import com.example.graphqlusers.service.UserService;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceTest {
    @Test
    void registerLoginMeFlow() {
        UserRepository userRepository = new InMemoryUserRepository();
        InMemoryRefreshTokenStore refreshTokenStore = new InMemoryRefreshTokenStore();
        JwtService jwtService = new JwtService("dev-secret-change-me-min-32-chars", Duration.ofMinutes(15));
        AuthService authService = new AuthService(userRepository, jwtService, refreshTokenStore);
        UserService userService = new UserService(userRepository);

        AuthPayload registerPayload = authService.register(new RegisterInput(
                "alice",
                "alice@example.com",
                "Alice Doe",
                "password123"
        ));
        assertNotNull(registerPayload.accessToken());

        AuthPayload loginPayload = authService.login("alice", "password123");
        AuthContext authContext = jwtService.parse(loginPayload.accessToken());
        assertEquals("alice", userService.getUser(authContext.userId()).username());
    }

    @Test
    void loginWithWrongPasswordFails() {
        UserRepository userRepository = new InMemoryUserRepository();
        InMemoryRefreshTokenStore refreshTokenStore = new InMemoryRefreshTokenStore();
        JwtService jwtService = new JwtService("dev-secret-change-me-min-32-chars", Duration.ofMinutes(15));
        AuthService authService = new AuthService(userRepository, jwtService, refreshTokenStore);

        authService.register(new RegisterInput(
                "bob",
                "bob@example.com",
                "Bob Doe",
                "password123"
        ));

        AppException exception = assertThrows(AppException.class, () -> authService.login("bob", "wrong"));
        assertEquals(ErrorCodes.UNAUTHORIZED, exception.getCode());
    }

    @Test
    void refreshTokenRotates() {
        UserRepository userRepository = new InMemoryUserRepository();
        InMemoryRefreshTokenStore refreshTokenStore = new InMemoryRefreshTokenStore();
        JwtService jwtService = new JwtService("dev-secret-change-me-min-32-chars", Duration.ofMinutes(15));
        AuthService authService = new AuthService(userRepository, jwtService, refreshTokenStore);

        AuthPayload payload = authService.register(new RegisterInput(
                "carol",
                "carol@example.com",
                "Carol Doe",
                "password123"
        ));
        AuthPayload refreshed = authService.refresh(payload.refreshToken());
        assertNotNull(refreshed.accessToken());
        assertNotNull(refreshed.refreshToken());
    }
}
