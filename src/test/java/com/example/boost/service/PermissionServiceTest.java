package com.example.boost.service;

import com.example.boost.domain.dto.PermissionCreateRequest;
import com.example.boost.domain.entity.Permission;
import com.example.boost.exception.NotFoundException;
import com.example.boost.repository.PermissionRepository;
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

    @InjectMocks
    private PermissionService permissionService;

    @Test
    void createPermissionSavesEntity() {
        PermissionCreateRequest request = new PermissionCreateRequest();
        request.setCode("USER_READ");
        request.setName("User Read");

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
