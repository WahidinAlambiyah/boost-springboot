package com.example.boost.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MenuAdminUpdateRequest {
    private UUID parentId;
    private String name;
    private String path;
    private String icon;
    private Integer orderNo;
    private Boolean isActive;
    private Boolean isVisible;
}
