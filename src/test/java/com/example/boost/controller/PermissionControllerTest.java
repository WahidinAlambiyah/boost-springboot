package com.example.boost.controller;

import com.example.boost.TestDataFactory;
import com.example.boost.domain.dto.PermissionCreateRequest;
import com.example.boost.domain.dto.PermissionResponse;
import com.example.boost.domain.dto.PermissionUpdateRequest;
import com.example.boost.domain.entity.Permission;
import com.example.boost.domain.mapper.PermissionMapper;
import com.example.boost.exception.NotFoundException;
import com.example.boost.security.JwtService;
import com.example.boost.service.AuditLogService;
import com.example.boost.service.PermissionService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.boost.security.MethodSecurityConfig.class)
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private PermissionMapper permissionMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    @WithMockUser(authorities = "PERMISSION_READ")
    void listPermissionsSuccess() throws Exception {
        Permission permission = TestDataFactory.permission();
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setCode(permission.getCode());
        when(permissionService.getPermissions()).thenReturn(List.of(permission));
        when(permissionMapper.toResponse(permission)).thenReturn(response);

        mockMvc.perform(get("/api/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("USER_READ"));
    }

    @Test
    @WithMockUser(authorities = "PERMISSION_READ")
    void listPermissionsPageSuccess() throws Exception {
        Permission permission = TestDataFactory.permission();
        when(permissionService.getPermissions(any())).thenReturn(new PageImpl<>(List.of(permission), PageRequest.of(0, 20), 1));
        when(permissionMapper.toResponse(permission)).thenReturn(new PermissionResponse());

        mockMvc.perform(get("/api/permissions/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(authorities = "PERMISSION_READ")
    void getPermissionNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(permissionService.getById(id)).thenThrow(new NotFoundException("Permission not found"));

        mockMvc.perform(get("/api/permissions/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "PERMISSION_WRITE")
    void createPermissionSuccess() throws Exception {
        Permission permission = TestDataFactory.permission();
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setCode(permission.getCode());
        when(permissionService.createPermission(any(PermissionCreateRequest.class))).thenReturn(permission);
        when(permissionMapper.toResponse(permission)).thenReturn(response);

        mockMvc.perform(post("/api/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.permissionCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("USER_READ"));
    }

    @Test
    @WithMockUser(authorities = "PERMISSION_WRITE")
    void createPermissionValidationError() throws Exception {
        mockMvc.perform(post("/api/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "PERMISSION_WRITE")
    void updatePermissionNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(permissionService.updatePermission(eq(id), any(PermissionUpdateRequest.class)))
                .thenThrow(new NotFoundException("Permission not found"));

        mockMvc.perform(put("/api/permissions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.permissionUpdateRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "PERMISSION_DELETE")
    void deletePermissionSuccess() throws Exception {
        mockMvc.perform(delete("/api/permissions/{id}", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Permission deleted"));
    }

    @Test
    void listPermissionsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/permissions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void listPermissionsForbidden() throws Exception {
        mockMvc.perform(get("/api/permissions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "PERMISSION_READ")
    void listPermissionsServerError() throws Exception {
        when(permissionService.getPermissions()).thenThrow(new RuntimeException("boom"));
        mockMvc.perform(get("/api/permissions"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }
}
