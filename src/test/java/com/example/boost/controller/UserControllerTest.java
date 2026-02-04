package com.example.boost.controller;

import com.example.boost.TestDataFactory;
import com.example.boost.domain.dto.UserCreateRequest;
import com.example.boost.domain.dto.UserDisableRequest;
import com.example.boost.domain.dto.UserPasswordUpdateRequest;
import com.example.boost.domain.dto.UserResponse;
import com.example.boost.domain.dto.UserRolesUpdateRequest;
import com.example.boost.domain.dto.UserUpdateRequest;
import com.example.boost.domain.entity.User;
import com.example.boost.domain.mapper.UserMapper;
import com.example.boost.exception.NotFoundException;
import com.example.boost.security.JwtService;
import com.example.boost.service.AuditLogService;
import com.example.boost.service.UserService;
import com.example.boost.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.boost.security.MethodSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    @WithMockUser(authorities = "USER_READ")
    void getUsersSuccess() throws Exception {
        User user = TestDataFactory.user();
        when(userService.getUsers(any(), nullable(String.class)))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1));
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        when(userMapper.toResponse(user)).thenReturn(response);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].username").value("demo"));
    }

    @Test
    void getUsersUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void getUserSuccess() throws Exception {
        User user = TestDataFactory.user();
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        when(userService.getById(user.getId())).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        mockMvc.perform(get("/api/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("demo"));
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void getUserNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getById(id)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @WithMockUser(authorities = "USER_WRITE")
    void createUserSuccess() throws Exception {
        UserCreateRequest request = TestDataFactory.userCreateRequest();
        User user = TestDataFactory.user();
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value("demo@example.com"));
    }

    @Test
    @WithMockUser(authorities = "USER_WRITE")
    void createUserValidationError() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @WithMockUser(authorities = "USER_WRITE")
    void updateUserNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.updateUser(eq(id), any(UserUpdateRequest.class)))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.userUpdateRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "USER_WRITE")
    void assignRolesSuccess() throws Exception {
        User user = TestDataFactory.user();
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setRoles(Set.of("ADMIN"));
        when(userService.assignRoles(eq(user.getId()), any())).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserRolesUpdateRequest request = new UserRolesUpdateRequest();
        request.setRoleCodes(Set.of("ADMIN"));

        mockMvc.perform(put("/api/users/{id}/roles", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0]").value("ADMIN"));
    }

    @Test
    @WithMockUser(authorities = "USER_DELETE")
    void deleteUserSuccess() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "USER_WRITE")
    void updatePasswordSuccess() throws Exception {
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest();
        request.setNewPassword("Password123!");

        mockMvc.perform(put("/api/users/{id}/password", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "USER_DISABLE")
    void disableUserSuccess() throws Exception {
        UserDisableRequest request = new UserDisableRequest();
        request.setReason("policy");

        mockMvc.perform(post("/api/users/{id}/disable", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void enableUserForbiddenWithoutAuthority() throws Exception {
        mockMvc.perform(post("/api/users/{id}/enable", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER_UNLOCK")
    void unlockUserSuccess() throws Exception {
        mockMvc.perform(post("/api/users/{id}/unlock", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void getUsersServerError() throws Exception {
        when(userService.getUsers(any(), nullable(String.class))).thenThrow(new RuntimeException("boom"));
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }
}
