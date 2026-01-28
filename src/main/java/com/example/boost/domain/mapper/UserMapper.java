package com.example.boost.domain.mapper;

import com.example.boost.domain.dto.UserResponse;
import com.example.boost.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setActive(user.isActive());
        response.setRoles(user.getRoleCodes());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
