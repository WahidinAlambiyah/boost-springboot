package com.example.boost.context;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestContextData {
    private final String requestId;
    private final String ip;
    private final String userAgent;
}
