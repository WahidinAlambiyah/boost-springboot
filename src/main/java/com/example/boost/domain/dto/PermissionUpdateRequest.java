package com.example.boost.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionUpdateRequest {
    private String name;
    private String module;
    private String description;
    private Boolean isActive;
}
