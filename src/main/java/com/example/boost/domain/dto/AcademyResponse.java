package com.example.boost.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class AcademyResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private String phone;
    private String email;
    private boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
