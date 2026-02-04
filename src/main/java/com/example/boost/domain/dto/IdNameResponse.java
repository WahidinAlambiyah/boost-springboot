package com.example.boost.domain.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdNameResponse {
    private UUID id;
    private String name;
}
