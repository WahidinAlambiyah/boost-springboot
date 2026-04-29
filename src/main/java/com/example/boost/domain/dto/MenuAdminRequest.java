package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MenuAdminRequest {
    private UUID parentId;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String path;
    private String icon;

    @NotNull
    private Integer orderNo;

    private Boolean isActive;
    private Boolean isVisible;
}
