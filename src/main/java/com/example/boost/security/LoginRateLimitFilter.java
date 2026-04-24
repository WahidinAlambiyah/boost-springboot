package com.example.boost.security;

import com.example.boost.context.RequestContext;
import com.example.boost.context.RequestContextData;
import com.example.boost.common.api.ApiResponse;
import com.example.boost.iam.application.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofSeconds(60);
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String REGISTER_PATH = "/api/auth/register";

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    public LoginRateLimitFilter(ObjectMapper objectMapper, AuditLogService auditLogService) {
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !LOGIN_PATH.equals(path) && !REGISTER_PATH.equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = resolveClientIp(request);
        String path = request.getRequestURI();
        if (isRateLimited(path, ip)) {
            String event = LOGIN_PATH.equals(path) ? "LOGIN_RATE_LIMITED" : "REGISTER_RATE_LIMITED";
            String message = LOGIN_PATH.equals(path)
                    ? "Too many login attempts. Please try again later."
                    : "Too many registration attempts. Please try again later.";
            auditLogService.securityEvent(event)
                    .statusFailure("Rate limit exceeded")
                    .metadata(Map.of("ip", ip))
                    .save();
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            ApiResponse<Object> body = ApiResponse.error(HttpStatus.TOO_MANY_REQUESTS.value(), message);
            objectMapper.writeValue(response.getOutputStream(), body);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String path, String ip) {
        long now = System.currentTimeMillis();
        String key = path + "|" + ip;
        Window window = windows.computeIfAbsent(key, value -> new Window(now));
        synchronized (window) {
            if (now - window.startMillis >= WINDOW.toMillis()) {
                window.startMillis = now;
                window.count = 0;
            }
            window.count++;
            return window.count > MAX_REQUESTS;
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        RequestContextData contextData = RequestContext.get();
        if (contextData != null && contextData.getIp() != null) {
            return contextData.getIp();
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String first = forwardedFor.split(",")[0].trim();
            if (!first.isBlank()) {
                return first;
            }
        }
        return request.getRemoteAddr();
    }

    private static final class Window {
        private long startMillis;
        private int count;

        private Window(long startMillis) {
            this.startMillis = startMillis;
        }
    }
}
