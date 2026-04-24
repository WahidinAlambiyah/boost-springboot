package com.example.boost.controller;

import com.example.boost.iam.api.RoleController;

import com.example.boost.TestDataFactory;
import com.example.boost.domain.dto.RoleCreateRequest;
import com.example.boost.domain.dto.RolePermissionsUpdateRequest;
import com.example.boost.domain.dto.RoleResponse;
import com.example.boost.domain.dto.RoleUpdateRequest;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.security.JwtService;
import com.example.boost.iam.application.AuditLogService;
import com.example.boost.iam.application.RoleService;
import com.example.boost.iam.application.TokenService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.boost.security.MethodSecurityConfig.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    @WithMockUser(authorities = "ROLE_READ")
    void getRolesSuccess() throws Exception {
        RoleResponse response = new RoleResponse();
        response.setId(UUID.randomUUID());
        response.setCode("ADMIN");
        response.setPermissions(Set.of("USER_READ"));
        when(roleService.getRoles(any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].code").value("ADMIN"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_READ")
    void getRoleNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(roleService.getById(id)).thenThrow(new NotFoundException("Role not found"));

        mockMvc.perform(get("/api/roles/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_WRITE")
    void createRoleSuccess() throws Exception {
        RoleResponse response = new RoleResponse();
        response.setId(UUID.randomUUID());
        response.setCode("ADMIN");
        when(roleService.createRole(any(RoleCreateRequest.class))).thenReturn(response);

        RoleCreateRequest request = TestDataFactory.roleCreateRequest();

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("ADMIN"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_WRITE")
    void createRoleValidationError() throws Exception {
        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_WRITE")
    void updateRoleNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(roleService.updateRole(eq(id), any(RoleUpdateRequest.class)))
                .thenThrow(new NotFoundException("Role not found"));

        mockMvc.perform(put("/api/roles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.roleUpdateRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_DELETE")
    void deleteRoleSuccess() throws Exception {
        mockMvc.perform(delete("/api/roles/{id}", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "ROLE_WRITE")
    void assignPermissionsSuccess() throws Exception {
        RoleResponse response = new RoleResponse();
        response.setId(UUID.randomUUID());
        response.setCode("ADMIN");
        response.setPermissions(Set.of("USER_READ"));
        when(roleService.assignPermissions(eq(response.getId()), any())).thenReturn(response);

        RolePermissionsUpdateRequest request = new RolePermissionsUpdateRequest();
        request.setPermissionCodes(Set.of("USER_READ"));

        mockMvc.perform(put("/api/roles/{id}/permissions", response.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permissions[0]").value("USER_READ"));
    }

    @Test
    void getRolesUnauthorized() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void getRolesForbidden() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_READ")
    void getRolesServerError() throws Exception {
        when(roleService.getRoles(any())).thenThrow(new RuntimeException("boom"));
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }
}
