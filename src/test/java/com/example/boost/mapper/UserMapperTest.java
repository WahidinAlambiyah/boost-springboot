package com.example.boost.mapper;

import com.example.boost.domain.dto.UserResponse;
import com.example.boost.domain.entity.User;
import com.example.boost.domain.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void mapsUserFields() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("demo");
        user.setEmail("demo@example.com");
        user.setActive(true);
        user.setCreatedAt(OffsetDateTime.now());

        UserMapper mapper = new UserMapper();
        UserResponse response = mapper.toResponse(user);

        assertThat(response.getUsername()).isEqualTo("demo");
        assertThat(response.isActive()).isTrue();
    }
}
