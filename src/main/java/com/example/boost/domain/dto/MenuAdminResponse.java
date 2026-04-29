package com.example.boost.domain.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record MenuAdminResponse(
        UUID id,
        UUID parentId,
        String code,
        String name,
        String path,
        String icon,
        Integer orderNo,
        boolean isActive,
        boolean isVisible
) {}
