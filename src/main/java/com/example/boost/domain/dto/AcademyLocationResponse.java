package com.example.boost.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class AcademyLocationResponse {
    private UUID id;
    private UUID academyId;
    private String code;
    private String name;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String timezone;
    private String phone;
    private boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
