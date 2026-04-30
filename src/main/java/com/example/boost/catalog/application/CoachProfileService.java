package com.example.boost.catalog.application;

import com.example.boost.catalog.infrastructure.AcademyRepository;
import com.example.boost.catalog.infrastructure.CoachProfileRepository;
import com.example.boost.common.exception.ConflictException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.dto.CoachProfileCreateRequest;
import com.example.boost.domain.dto.CoachProfileResponse;
import com.example.boost.domain.dto.CoachProfileUpdateRequest;
import com.example.boost.domain.entity.Academy;
import com.example.boost.domain.entity.CoachProfile;
import com.example.boost.domain.entity.User;
import com.example.boost.iam.infrastructure.UserRepository;
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
public class CoachProfileService {
    private final CoachProfileRepository coachProfileRepository;
    private final AcademyRepository academyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ACADEMY_READ')")
    public Page<CoachProfileResponse> list(UUID academyId, String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return coachProfileRepository.findByAcademyId(academyId, pageable).map(this::toResponse);
        }
        return coachProfileRepository.findByAcademyIdAndCoachNoContainingIgnoreCaseOrAcademyIdAndFullNameContainingIgnoreCase(
                academyId, keyword, academyId, keyword, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ACADEMY_READ')")
    public CoachProfileResponse getById(UUID id, UUID academyId) {
        return toResponse(coachProfileRepository.getActiveByIdAndAcademyIdOrThrow(id, academyId));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ACADEMY_WRITE')")
    public CoachProfileResponse create(CoachProfileCreateRequest request) {
        Academy academy = academyRepository.getActiveByIdOrThrow(request.getAcademyId());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isActive() || user.getDeletedAt() != null) {
            throw new ConflictException("User is inactive");
        }

        coachProfileRepository.ensureActiveUniqueCoachUser(request.getAcademyId(), request.getUserId());

        CoachProfile coachProfile = new CoachProfile();
        coachProfile.setAcademy(academy);
        coachProfile.setUser(user);
        coachProfile.setCoachNo(request.getCoachNo());
        coachProfile.setFullName(request.getFullName());
        coachProfile.setPhone(request.getPhone());
        coachProfile.setSpecialties(request.getSpecialties());
        coachProfile.setBio(request.getBio());
        coachProfile.setActive(true);

        return toResponse(coachProfileRepository.save(coachProfile));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ACADEMY_WRITE')")
    public CoachProfileResponse update(UUID id, UUID academyId, CoachProfileUpdateRequest request) {
        CoachProfile coachProfile = coachProfileRepository.getActiveByIdAndAcademyIdOrThrow(id, academyId);
        coachProfile.setFullName(request.getFullName());
        coachProfile.setPhone(request.getPhone());
        coachProfile.setSpecialties(request.getSpecialties());
        coachProfile.setBio(request.getBio());
        return toResponse(coachProfileRepository.save(coachProfile));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ACADEMY_DELETE')")
    public void delete(UUID id, UUID academyId) {
        CoachProfile coachProfile = coachProfileRepository.getActiveByIdAndAcademyIdOrThrow(id, academyId);
        if (!coachProfile.isActive()) {
            throw new ConflictException("Coach profile already inactive");
        }
        coachProfile.setActive(false);
        coachProfile.setDeletedAt(OffsetDateTime.now());
        coachProfileRepository.save(coachProfile);
    }

    private CoachProfileResponse toResponse(CoachProfile coachProfile) {
        CoachProfileResponse response = new CoachProfileResponse();
        response.setId(coachProfile.getId());
        response.setAcademyId(coachProfile.getAcademy().getId());
        response.setUserId(coachProfile.getUser().getId());
        response.setCoachNo(coachProfile.getCoachNo());
        response.setFullName(coachProfile.getFullName());
        response.setPhone(coachProfile.getPhone());
        response.setSpecialties(coachProfile.getSpecialties());
        response.setBio(coachProfile.getBio());
        response.setActive(coachProfile.isActive());
        response.setCreatedAt(coachProfile.getCreatedAt());
        response.setUpdatedAt(coachProfile.getUpdatedAt());
        return response;
    }
}
