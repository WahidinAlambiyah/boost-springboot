package com.example.boost.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PermissionResponse {
    private UUID id;
    private String code;
    private String name;
    private String module;
    @JsonProperty("isActive")
    private boolean isActive;
}
