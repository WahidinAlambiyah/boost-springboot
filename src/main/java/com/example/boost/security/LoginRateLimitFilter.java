package com.example.boost.security;

import com.example.boost.context.RequestContext;
import com.example.boost.context.RequestContextData;
import com.example.boost.domain.dto.ApiResponse;
import com.example.boost.service.AuditLogService;
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

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    public LoginRateLimitFilter(ObjectMapper objectMapper, AuditLogService auditLogService) {
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !LOGIN_PATH.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = resolveClientIp(request);
        if (isRateLimited(ip)) {
            auditLogService.securityEvent("LOGIN_RATE_LIMITED")
                    .statusFailure("Rate limit exceeded")
                    .metadata(Map.of("ip", ip))
                    .save();
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            ApiResponse<Object> body = ApiResponse.error(HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Too many login attempts. Please try again later.");
            objectMapper.writeValue(response.getOutputStream(), body);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String ip) {
        long now = System.currentTimeMillis();
        Window window = windows.computeIfAbsent(ip, key -> new Window(now));
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
