package com.example.boost.domain.mapper;

import com.example.boost.domain.dto.UserResponse;
import com.example.boost.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
