package com.example.boost.assessment.application;

import com.example.boost.assessment.infrastructure.AssessmentSkillRepository;
import com.example.boost.catalog.infrastructure.AcademyRepository;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.dto.AssessmentSkillCreateRequest;
import com.example.boost.domain.dto.AssessmentSkillResponse;
import com.example.boost.domain.dto.AssessmentSkillUpdateRequest;
import com.example.boost.domain.entity.Academy;
import com.example.boost.domain.entity.AssessmentSkill;
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
public class AssessmentSkillService {
    private final AssessmentSkillRepository assessmentSkillRepository;
    private final AcademyRepository academyRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ASSESSMENT_READ')")
    public Page<AssessmentSkillResponse> list(UUID academyId, Pageable pageable) {
        return assessmentSkillRepository.findForList(academyId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ASSESSMENT_READ')")
    public AssessmentSkillResponse getById(UUID id) {
        return toResponse(getActiveByIdOrThrow(id));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ASSESSMENT_WRITE')")
    public AssessmentSkillResponse create(AssessmentSkillCreateRequest request) {
        AssessmentSkill skill = new AssessmentSkill();
        skill.setAcademy(resolveAcademy(request.getAcademyId()));
        skill.setCode(request.getCode().trim());
        skill.setName(request.getName().trim());
        skill.setDescription(request.getDescription());
        skill.setOrderNo(request.getOrderNo());
        skill.setActive(request.isActive());
        skill.setMaxScore(request.getMaxScore());
        return toResponse(assessmentSkillRepository.save(skill));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ASSESSMENT_WRITE')")
    public AssessmentSkillResponse update(UUID id, AssessmentSkillUpdateRequest request) {
        AssessmentSkill skill = getActiveByIdOrThrow(id);
        skill.setAcademy(resolveAcademy(request.getAcademyId()));
        skill.setName(request.getName().trim());
        skill.setDescription(request.getDescription());
        skill.setOrderNo(request.getOrderNo());
        skill.setActive(request.isActive());
        skill.setMaxScore(request.getMaxScore());
        return toResponse(assessmentSkillRepository.save(skill));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ASSESSMENT_WRITE')")
    public void delete(UUID id) {
        AssessmentSkill skill = getActiveByIdOrThrow(id);
        skill.setDeletedAt(OffsetDateTime.now());
        assessmentSkillRepository.save(skill);
    }

    private Academy resolveAcademy(UUID academyId) {
        if (academyId == null) {
            return null;
        }
        return academyRepository.getActiveByIdOrThrow(academyId);
    }

    private AssessmentSkill getActiveByIdOrThrow(UUID id) {
        return assessmentSkillRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assessment skill not found"));
    }

    private AssessmentSkillResponse toResponse(AssessmentSkill skill) {
        AssessmentSkillResponse response = new AssessmentSkillResponse();
        response.setId(skill.getId());
        response.setAcademyId(skill.getAcademy() != null ? skill.getAcademy().getId() : null);
        response.setCode(skill.getCode());
        response.setName(skill.getName());
        response.setDescription(skill.getDescription());
        response.setOrderNo(skill.getOrderNo());
        response.setActive(skill.isActive());
        response.setMaxScore(skill.getMaxScore());
        response.setCreatedAt(skill.getCreatedAt());
        response.setUpdatedAt(skill.getUpdatedAt());
        return response;
    }
}
