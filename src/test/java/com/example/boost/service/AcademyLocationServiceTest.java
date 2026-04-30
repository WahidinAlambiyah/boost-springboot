package com.example.boost.service;

import com.example.boost.catalog.application.AcademyLocationService;
import com.example.boost.catalog.infrastructure.AcademyLocationRepository;
import com.example.boost.catalog.infrastructure.AcademyRepository;
import com.example.boost.common.exception.ConflictException;
import com.example.boost.domain.dto.AcademyLocationCreateRequest;
import com.example.boost.domain.dto.AcademyLocationUpdateRequest;
import com.example.boost.domain.entity.Academy;
import com.example.boost.domain.entity.AcademyLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademyLocationServiceTest {

    @Mock
    private AcademyLocationRepository academyLocationRepository;

    @Mock
    private AcademyRepository academyRepository;

    @InjectMocks
    private AcademyLocationService academyLocationService;

    @Test
    void createAndUpdateAndDeleteFollowSoftDeleteBehavior() {
        UUID academyId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();

        Academy academy = new Academy();
        academy.setId(academyId);
        when(academyRepository.getActiveByIdOrThrow(academyId)).thenReturn(academy);

        when(academyLocationRepository.save(any(AcademyLocation.class))).thenAnswer(invocation -> {
            AcademyLocation location = invocation.getArgument(0);
            if (location.getId() == null) {
                location.setId(locationId);
            }
            return location;
        });

        AcademyLocationCreateRequest createRequest = new AcademyLocationCreateRequest();
        createRequest.setAcademyId(academyId);
        createRequest.setCode("JKT01");
        createRequest.setName("Jakarta Center");
        createRequest.setAddress("Jl. Sudirman 1");
        createRequest.setCity("Jakarta");
        createRequest.setState("DKI Jakarta");
        createRequest.setPostalCode("10220");
        createRequest.setCountry("ID");
        createRequest.setTimezone("Asia/Jakarta");
        createRequest.setPhone("+6221123456");

        var created = academyLocationService.create(createRequest);
        assertThat(created.getId()).isEqualTo(locationId);
        assertThat(created.isActive()).isTrue();

        AcademyLocation persisted = new AcademyLocation();
        persisted.setId(locationId);
        persisted.setAcademy(academy);
        persisted.setCode("JKT01");
        persisted.setActive(true);
        when(academyLocationRepository.getActiveByIdOrThrow(locationId)).thenReturn(persisted);

        AcademyLocationUpdateRequest updateRequest = new AcademyLocationUpdateRequest();
        updateRequest.setName("Jakarta HQ");
        updateRequest.setAddress("Jl. Sudirman 2");
        updateRequest.setCity("Jakarta");
        updateRequest.setState("DKI Jakarta");
        updateRequest.setPostalCode("10220");
        updateRequest.setCountry("ID");
        updateRequest.setTimezone("Asia/Jakarta");
        updateRequest.setPhone("+6221765432");

        var updated = academyLocationService.update(locationId, updateRequest);
        assertThat(updated.getName()).isEqualTo("Jakarta HQ");

        academyLocationService.delete(locationId);
        assertThat(persisted.isActive()).isFalse();
        assertThat(persisted.getDeletedAt()).isNotNull();
    }

    @Test
    void createUniqueConstraintConflictPathThrowsConflictException() {
        UUID academyId = UUID.randomUUID();
        Academy academy = new Academy();
        academy.setId(academyId);
        when(academyRepository.getActiveByIdOrThrow(academyId)).thenReturn(academy);
        doThrow(new ConflictException("Academy location code already exists"))
                .when(academyLocationRepository).ensureActiveCodeUnique(academyId, "JKT01");

        AcademyLocationCreateRequest createRequest = new AcademyLocationCreateRequest();
        createRequest.setAcademyId(academyId);
        createRequest.setCode("JKT01");
        createRequest.setName("Jakarta Center");
        createRequest.setAddress("Jl. Sudirman 1");
        createRequest.setCity("Jakarta");
        createRequest.setState("DKI Jakarta");
        createRequest.setPostalCode("10220");
        createRequest.setCountry("ID");
        createRequest.setTimezone("Asia/Jakarta");

        assertThatThrownBy(() -> academyLocationService.create(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(academyLocationRepository, never()).save(any());
    }
}
