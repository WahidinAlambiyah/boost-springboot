package com.example.boost.integration;

import com.example.boost.domain.dto.RoleCreateRequest;
import com.example.boost.domain.dto.RolePermissionsUpdateRequest;
import com.example.boost.domain.dto.RoleUpdateRequest;
import com.example.boost.domain.dto.UserCreateRequest;
import com.example.boost.domain.dto.UserRolesUpdateRequest;
import com.example.boost.domain.entity.Permission;
import com.example.boost.domain.entity.Role;
import com.example.boost.domain.entity.RolePermission;
import com.example.boost.domain.entity.RolePermissionId;
import com.example.boost.domain.entity.User;
import com.example.boost.iam.infrastructure.PermissionRepository;
import com.example.boost.iam.infrastructure.RoleRepository;
import com.example.boost.iam.infrastructure.UserRepository;
import com.example.boost.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=fastworks_springboot",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema.sql",
        "spring.liquibase.enabled=false",
        "DB_SCHEMA=public",
        "spring.datasource.hikari.schema=public",
        "spring.datasource.hikari.connection-init-sql="
})
@AutoConfigureMockMvc(addFilters = false)
class LazyInitializationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    private User seededUser;
    private Role seededRole;
    private Permission seededPermission;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        Permission permission = new Permission();
        permission.setCode("USER_READ");
        permission.setName("User Read");
        permission.setModule("USER");
        permission.setActive(true);
        seededPermission = permissionRepository.saveAndFlush(permission);

        Role role = new Role();
        role.setCode("USER");
        role.setName("User");
        role.setActive(true);
        seededRole = roleRepository.saveAndFlush(role);

        RolePermission rolePermission = RolePermission.builder()
                .role(seededRole)
                .permission(seededPermission)
                .assignedAt(java.time.OffsetDateTime.now())
                .id(new RolePermissionId(seededRole.getId(), seededPermission.getId()))
                .build();
        seededRole.getRolePermissions().add(rolePermission);
        seededRole = roleRepository.saveAndFlush(seededRole);

        User user = new User();
        user.setUsername("demo");
        user.setEmail("demo@example.com");
        user.setPasswordHash("hashed");
        user.setActive(true);
        user.setRoles(Set.of(seededRole));
        seededUser = userRepository.saveAndFlush(user);
    }

    @Test
    @WithMockUser(authorities = "USER_WRITE")
    void createUserDoesNotTriggerLazyInitialization() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("new-user");
        request.setEmail("new@example.com");
        request.setPassword("Password123!");
        request.setRoleCodes(Set.of("USER"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.identifier").value("new@example.com"));
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void getUserByIdReturnsPermissionsSafely() throws Exception {
        mockMvc.perform(get("/api/users/{id}", seededUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permissions[0]").value("USER_READ"));
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void getCurrentUserUsesTransactionalMapping() throws Exception {
        UserPrincipal principal = new UserPrincipal(seededUser, List.of("USER"), List.of("USER_READ"));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                "n/a",
                principal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            mockMvc.perform(get("/api/users/me")
                            .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.username").value("demo"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @WithMockUser(authorities = "ROLE_WRITE")
    void createAndUpdateRoleWorksWithDtoMapping() throws Exception {
        RoleCreateRequest createRequest = new RoleCreateRequest();
        createRequest.setCode("ADMIN");
        createRequest.setName("Admin");
        createRequest.setIsActive(true);

        String responseBody = mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("ADMIN"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID createdRoleId = UUID.fromString(objectMapper.readTree(responseBody).path("data").path("id").asText());

        RoleUpdateRequest updateRequest = new RoleUpdateRequest();
        updateRequest.setName("Admin Updated");

        mockMvc.perform(put("/api/roles/{id}", createdRoleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Admin Updated"));
    }

    @Test
    @WithMockUser(authorities = "USER_WRITE")
    void assignRoleToUserKeepsLazyRelationsSafe() throws Exception {
        UserRolesUpdateRequest request = new UserRolesUpdateRequest();
        request.setRoleCodes(Set.of("USER"));

        mockMvc.perform(put("/api/users/{id}/roles", seededUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0]").value("USER"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_WRITE")
    void assignPermissionToRoleKeepsLazyRelationsSafe() throws Exception {
        RolePermissionsUpdateRequest request = new RolePermissionsUpdateRequest();
        request.setPermissionCodes(Set.of("USER_READ"));

        mockMvc.perform(put("/api/roles/{id}/permissions", seededRole.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permissions[0]").value("USER_READ"));
    }
}
