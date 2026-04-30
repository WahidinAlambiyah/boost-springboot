package com.example.boost.catalog.application;

import com.example.boost.catalog.infrastructure.AcademyRepository;
import com.example.boost.common.exception.ConflictException;
import com.example.boost.domain.dto.AcademyCreateRequest;
import com.example.boost.domain.dto.AcademyResponse;
import com.example.boost.domain.dto.AcademyUpdateRequest;
import com.example.boost.domain.entity.Academy;
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
public class AcademyService {
    private final AcademyRepository academyRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ACADEMY_READ')")
    public Page<AcademyResponse> list(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return academyRepository.findAll(pageable).map(this::toResponse);
        }
        return academyRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ACADEMY_READ')")
    public AcademyResponse getById(UUID id) {
        return toResponse(academyRepository.getActiveByIdOrThrow(id));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ACADEMY_WRITE')")
    public AcademyResponse create(AcademyCreateRequest request) {
        academyRepository.ensureActiveCodeUnique(request.getCode());

        Academy academy = new Academy();
        academy.setCode(request.getCode());
        academy.setName(request.getName());
        academy.setDescription(request.getDescription());
        academy.setPhone(request.getPhone());
        academy.setEmail(request.getEmail());
        academy.setActive(true);

        return toResponse(academyRepository.save(academy));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ACADEMY_WRITE')")
    public AcademyResponse update(UUID id, AcademyUpdateRequest request) {
        Academy academy = academyRepository.getActiveByIdOrThrow(id);
        academy.setName(request.getName());
        academy.setDescription(request.getDescription());
        academy.setPhone(request.getPhone());
        academy.setEmail(request.getEmail());
        return toResponse(academyRepository.save(academy));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ACADEMY_DELETE')")
    public void delete(UUID id) {
        Academy academy = academyRepository.getActiveByIdOrThrow(id);
        if (!academy.isActive()) {
            throw new ConflictException("Academy already inactive");
        }
        academy.setActive(false);
        academy.setDeletedAt(OffsetDateTime.now());
        academyRepository.save(academy);
    }

    private AcademyResponse toResponse(Academy academy) {
        AcademyResponse response = new AcademyResponse();
        response.setId(academy.getId());
        response.setCode(academy.getCode());
        response.setName(academy.getName());
        response.setDescription(academy.getDescription());
        response.setPhone(academy.getPhone());
        response.setEmail(academy.getEmail());
        response.setActive(academy.isActive());
        response.setCreatedAt(academy.getCreatedAt());
        response.setUpdatedAt(academy.getUpdatedAt());
        return response;
    }
}
