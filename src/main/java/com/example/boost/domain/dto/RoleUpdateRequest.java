package com.example.boost.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleUpdateRequest {
    private String name;
    private String description;
    private Boolean isActive;
}
