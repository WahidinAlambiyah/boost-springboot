package com.example.purchaseorder.dto;

import com.example.purchaseorder.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String createdBy;
    private OffsetDateTime createdDatetime;
    private String updatedBy;
    private OffsetDateTime updatedDatetime;

    public static UserResponse from(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdBy(user.getCreatedBy())
                .createdDatetime(user.getCreatedDatetime())
                .updatedBy(user.getUpdatedBy())
                .updatedDatetime(user.getUpdatedDatetime())
                .build();
    }
}
