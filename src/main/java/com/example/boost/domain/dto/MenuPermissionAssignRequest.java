package com.example.boost.domain.dto;

import com.example.boost.domain.entity.MenuMatchMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MenuPermissionAssignRequest {
    @NotNull
    private UUID permissionId;

    private MenuMatchMode matchMode;
}
