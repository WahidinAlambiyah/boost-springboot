package com.alambiyah.userauth.framework.mapper;

import com.alambiyah.userauth.domain.entity.User;
import com.alambiyah.userauth.dto.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.stream.Collectors;

@ApplicationScoped
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.id,
                user.username,
                user.email,
                user.fullName,
                user.roles.stream().map(Enum::name).collect(Collectors.toSet()),
                user.isActive,
                user.createdAt,
                user.updatedAt
        );
    }
}
