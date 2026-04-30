package com.example.boost.catalog.application;

import com.example.boost.catalog.infrastructure.AcademyLocationRepository;
import com.example.boost.catalog.infrastructure.AcademyRepository;
import com.example.boost.common.exception.ConflictException;
import com.example.boost.domain.dto.AcademyLocationCreateRequest;
import com.example.boost.domain.dto.AcademyLocationResponse;
import com.example.boost.domain.dto.AcademyLocationUpdateRequest;
import com.example.boost.domain.entity.Academy;
import com.example.boost.domain.entity.AcademyLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AcademyLocationService {
    private final AcademyLocationRepository academyLocationRepository;
    private final AcademyRepository academyRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ACADEMY_READ')")
    public Page<AcademyLocationResponse> list(UUID academyId, String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return academyLocationRepository.findByAcademyId(academyId, pageable).map(this::toResponse);
        }
        return academyLocationRepository
                .findByAcademyIdAndCodeContainingIgnoreCaseOrAcademyIdAndNameContainingIgnoreCase(
                        academyId, keyword, academyId, keyword, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ACADEMY_READ')")
    public AcademyLocationResponse getById(UUID id, UUID academyId) {
        return toResponse(academyLocationRepository.getActiveByIdAndAcademyIdOrThrow(id, academyId));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ACADEMY_WRITE')")
    public AcademyLocationResponse create(AcademyLocationCreateRequest request) {
        Academy academy = academyRepository.getActiveByIdOrThrow(request.getAcademyId());
        academyLocationRepository.ensureActiveCodeUnique(request.getAcademyId(), request.getCode());

        AcademyLocation academyLocation = new AcademyLocation();
        academyLocation.setAcademy(academy);
        academyLocation.setCode(request.getCode());
        academyLocation.setName(request.getName());
        academyLocation.setAddress(request.getAddress());
        academyLocation.setCity(request.getCity());
        academyLocation.setState(request.getState());
        academyLocation.setPostalCode(request.getPostalCode());
        academyLocation.setCountry(request.getCountry());
        academyLocation.setTimezone(request.getTimezone());
        academyLocation.setPhone(request.getPhone());
        academyLocation.setActive(true);

        return toResponse(academyLocationRepository.save(academyLocation));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ACADEMY_WRITE')")
    public AcademyLocationResponse update(UUID id, UUID academyId, AcademyLocationUpdateRequest request) {
        AcademyLocation academyLocation = academyLocationRepository.getActiveByIdAndAcademyIdOrThrow(id, academyId);
        academyLocation.setName(request.getName());
        academyLocation.setAddress(request.getAddress());
        academyLocation.setCity(request.getCity());
        academyLocation.setState(request.getState());
        academyLocation.setPostalCode(request.getPostalCode());
        academyLocation.setCountry(request.getCountry());
        academyLocation.setTimezone(request.getTimezone());
        academyLocation.setPhone(request.getPhone());
        return toResponse(academyLocationRepository.save(academyLocation));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ACADEMY_DELETE')")
    public void delete(UUID id, UUID academyId) {
        AcademyLocation academyLocation = academyLocationRepository.getActiveByIdAndAcademyIdOrThrow(id, academyId);
        if (!academyLocation.isActive()) {
            throw new ConflictException("Academy location already inactive");
        }
        academyLocation.setActive(false);
        academyLocation.setDeletedAt(OffsetDateTime.now());
        academyLocationRepository.save(academyLocation);
    }

    private AcademyLocationResponse toResponse(AcademyLocation academyLocation) {
        AcademyLocationResponse response = new AcademyLocationResponse();
        response.setId(academyLocation.getId());
        response.setAcademyId(academyLocation.getAcademy().getId());
        response.setCode(academyLocation.getCode());
        response.setName(academyLocation.getName());
        response.setAddress(academyLocation.getAddress());
        response.setCity(academyLocation.getCity());
        response.setState(academyLocation.getState());
        response.setPostalCode(academyLocation.getPostalCode());
        response.setCountry(academyLocation.getCountry());
        response.setTimezone(academyLocation.getTimezone());
        response.setPhone(academyLocation.getPhone());
        response.setActive(academyLocation.isActive());
        response.setCreatedAt(academyLocation.getCreatedAt());
        response.setUpdatedAt(academyLocation.getUpdatedAt());
        return response;
    }
}
