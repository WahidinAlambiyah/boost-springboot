package com.example.workorder.controller;

import com.example.workorder.security.UserRole;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
public class RequestContextResolver {

    public RequestContext resolve(HttpHeaders headers) {
        String roleHeader = headers.getFirst("X-ROLE");
        if (!StringUtils.hasText(roleHeader)) {
            throw new IllegalArgumentException("X-ROLE header is required");
        }
        UserRole role = UserRole.valueOf(roleHeader.toUpperCase());

        String divisionHeader = headers.getFirst("X-DIVISION-ID");
        Optional<Long> divisionId = Optional.empty();
        if (StringUtils.hasText(divisionHeader)) {
            divisionId = Optional.of(Long.parseLong(divisionHeader));
        }
        return new RequestContext(role, divisionId);
    }
}
