package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuReorderRequest {
    @NotNull
    private Integer orderNo;
}
