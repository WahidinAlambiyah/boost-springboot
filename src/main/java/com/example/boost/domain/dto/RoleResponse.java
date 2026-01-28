package com.example.boost.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class RoleResponse {
    private UUID id;
    private String code;
    private String name;
    private Set<String> permissions;
}
