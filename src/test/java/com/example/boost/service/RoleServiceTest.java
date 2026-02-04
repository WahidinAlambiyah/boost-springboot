package com.example.boost.service;

import com.example.boost.domain.dto.RoleCreateRequest;
import com.example.boost.domain.dto.RoleResponse;
import com.example.boost.domain.entity.AuditLog;
import com.example.boost.domain.entity.Permission;
import com.example.boost.domain.entity.Role;
import com.example.boost.domain.mapper.RoleMapper;
import com.example.boost.repository.PermissionRepository;
import com.example.boost.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleService roleService;

    @BeforeEach
    void setUpAuditLog() {
        AuditLogService.AuditLogBuilder builder = Mockito.mock(
                AuditLogService.AuditLogBuilder.class,
                Mockito.RETURNS_SELF
        );
        Mockito.lenient().when(builder.save()).thenReturn(new AuditLog());
        Mockito.lenient().when(auditLogService.securityEvent(any())).thenReturn(builder);
        Mockito.lenient().when(auditLogService.rbacEvent(any())).thenReturn(builder);
        Mockito.lenient().when(auditLogService.dataEvent(any())).thenReturn(builder);
    }

    @Test
    void createRoleSavesEntity() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setCode("ADMIN");
        request.setName("Admin");
        request.setIsActive(true);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleMapper.toResponse(any(Role.class))).thenReturn(new RoleResponse());

        roleService.createRole(request);

        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void assignPermissionsAddsRelations() {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode("ADMIN");
        role.setRolePermissions(new java.util.HashSet<>());
        when(roleRepository.findWithPermissionsById(role.getId())).thenReturn(java.util.Optional.of(role));
        Permission permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setCode("USER_READ");
        when(permissionRepository.findByCodeIn(Set.of("USER_READ"))).thenReturn(List.of(permission));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleMapper.toResponse(any(Role.class))).thenReturn(new RoleResponse());

        roleService.assignPermissions(role.getId(), Set.of("USER_READ"));

        verify(roleRepository).save(any(Role.class));
    }
}
