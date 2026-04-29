package com.example.boost.domain.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record MenuNodeResponse(
        UUID id,
        UUID parentId,
        String code,
        String label,
        String path,
        String icon,
        Integer orderNo,
        List<MenuNodeResponse> children
) {
}
