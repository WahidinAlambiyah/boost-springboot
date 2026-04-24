package com.example.boost.service;

import com.example.boost.iam.application.PermissionService;

import com.example.boost.domain.dto.PermissionCreateRequest;
import com.example.boost.domain.entity.Permission;
import com.example.boost.domain.mapper.PermissionMapper;
import com.example.boost.exception.NotFoundException;
import com.example.boost.iam.infrastructure.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    void createPermissionSavesEntity() {
        PermissionCreateRequest request = new PermissionCreateRequest();
        request.setCode("USER_READ");
        request.setName("User Read");
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(permissionMapper.toResponse(any(Permission.class)))
                .thenReturn(new com.example.boost.domain.dto.PermissionResponse());

        permissionService.createPermission(request);

        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    void getByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(permissionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.getById(id))
                .isInstanceOf(NotFoundException.class);
    }
}
