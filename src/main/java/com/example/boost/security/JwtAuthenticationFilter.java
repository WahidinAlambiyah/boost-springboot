package com.example.boost.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.boost.service.SessionService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final SessionService sessionService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   CustomUserDetailsService userDetailsService,
                                   SessionService sessionService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        if (log.isDebugEnabled()) {
            log.debug("JWT filter request: method={}, uri={}, hasAuthorizationHeader={}, hasToken={}",
                    request.getMethod(), request.getRequestURI(), header != null, token != null);
        }

        boolean hasValidToken = token != null && jwtTokenProvider.validateToken(token);
        if (log.isDebugEnabled()) {
            log.debug("JWT filter token valid: {}", hasValidToken);
        }

        if (hasValidToken && SecurityContextHolder.getContext().getAuthentication() == null) {
            String sessionId = jwtTokenProvider.getSessionId(token);
            Optional<UUID> sessionUserId = sessionService.getUserIdForSession(sessionId);
            if (sessionUserId.isPresent()) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (userDetails instanceof CustomUserDetails customUserDetails &&
                        !sessionUserId.get().equals(customUserDetails.getUserId())) {
                    log.warn("JWT session user mismatch: uri={}, tokenUserId={}, sessionUserId={}",
                            request.getRequestURI(), customUserDetails.getUserId(), sessionUserId.get());
                    filterChain.doFilter(request, response);
                    return;
                }
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                if (log.isDebugEnabled()) {
                    log.debug("JWT authentication set for user: {}", userDetails.getUsername());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
