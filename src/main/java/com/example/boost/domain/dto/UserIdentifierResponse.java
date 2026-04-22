package com.example.boost.domain.dto;

import lombok.Getter;

@Getter
public class UserIdentifierResponse {
    private final String identifier;

    public UserIdentifierResponse(String identifier) {
        this.identifier = identifier;
    }
}
