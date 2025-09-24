package com.example.purchaseorder.dto;

import com.example.purchaseorder.domain.User;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTest {

    @Test
    void fromShouldMapAllFields() {
        OffsetDateTime created = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updated = OffsetDateTime.now();
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("123456789")
                .createdBy("SYSTEM")
                .createdDatetime(created)
                .updatedBy("admin")
                .updatedDatetime(updated)
                .build();

        UserResponse response = UserResponse.from(user);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getPhone()).isEqualTo("123456789");
        assertThat(response.getCreatedBy()).isEqualTo("SYSTEM");
        assertThat(response.getCreatedDatetime()).isEqualTo(created);
        assertThat(response.getUpdatedBy()).isEqualTo("admin");
        assertThat(response.getUpdatedDatetime()).isEqualTo(updated);
    }

    @Test
    void fromShouldReturnNullWhenUserIsNull() {
        assertThat(UserResponse.from(null)).isNull();
    }
}
