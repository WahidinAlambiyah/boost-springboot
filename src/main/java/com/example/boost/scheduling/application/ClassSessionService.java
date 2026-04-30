package com.example.boost.scheduling.application;

import com.example.boost.catalog.infrastructure.ClassGroupRepository;
import com.example.boost.common.exception.BadRequestException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.dto.ClassSessionConflictCheckResponse;
import com.example.boost.domain.dto.ClassSessionCrudRequest;
import com.example.boost.domain.dto.ClassSessionResponse;
import com.example.boost.domain.entity.ClassSession;
import com.example.boost.domain.entity.ClassSessionCoach;
import com.example.boost.scheduling.infrastructure.ClassSessionCoachRepository;
import com.example.boost.scheduling.infrastructure.ClassSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassSessionService {
    private final ClassSessionRepository classSessionRepository;
    private final ClassGroupRepository classGroupRepository;
    private final ClassSessionCoachRepository classSessionCoachRepository;
    private final ClassSessionConflictService classSessionConflictService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('SCHEDULE_READ','SESSION_READ')")
    public List<ClassSessionResponse> list() {
        return classSessionRepository.findAllByDeletedAtIsNull().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('SCHEDULE_READ','SESSION_READ')")
    public ClassSessionResponse get(UUID id) {
        return toResponse(getActiveSession(id));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('SCHEDULE_WRITE','SESSION_WRITE')")
    public ClassSessionResponse create(ClassSessionCrudRequest request) {
        validateTime(request);
        var classGroup = classGroupRepository.findById(request.classGroupId())
                .orElseThrow(() -> new NotFoundException("Class group not found"));
        var conflicts = classSessionConflictService.checkForCreate(
                classGroup.getAcademy().getId(), request.sessionDate(), request.startTime(), request.endTime(), request.locationId(), request.coachIds());
        if (!conflicts.isEmpty()) {
            throw new BadRequestException("Session conflict detected: " + conflicts.get(0).getMessage());
        }

        ClassSession session = new ClassSession();
        session.setClassGroup(classGroup);
        session.setSessionDate(request.sessionDate());
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());
        session.setLocationId(request.locationId());
        session.setStatus(request.status() == null ? "SCHEDULED" : request.status());
        classSessionRepository.save(session);
        syncCoaches(session, request.coachIds());
        return toResponse(session);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('SCHEDULE_WRITE','SESSION_WRITE')")
    public ClassSessionResponse update(UUID id, ClassSessionCrudRequest request) {
        validateTime(request);
        ClassSession session = getActiveSession(id);
        var classGroup = classGroupRepository.findById(request.classGroupId())
                .orElseThrow(() -> new NotFoundException("Class group not found"));
        var conflicts = classSessionConflictService.checkForUpdate(
                id, classGroup.getAcademy().getId(), request.sessionDate(), request.startTime(), request.endTime(), request.locationId(), request.coachIds());
        if (!conflicts.isEmpty()) {
            throw new BadRequestException("Session conflict detected: " + conflicts.get(0).getMessage());
        }

        session.setClassGroup(classGroup);
        session.setSessionDate(request.sessionDate());
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());
        session.setLocationId(request.locationId());
        session.setStatus(request.status() == null ? session.getStatus() : request.status());
        classSessionRepository.save(session);
        syncCoaches(session, request.coachIds());
        return toResponse(session);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('SCHEDULE_WRITE','SESSION_WRITE')")
    public void softDelete(UUID id) {
        ClassSession session = getActiveSession(id);
        OffsetDateTime now = OffsetDateTime.now();
        session.setDeletedAt(now);
        session.setUpdatedAt(now);
        classSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('SCHEDULE_READ','SESSION_READ')")
    public ClassSessionConflictCheckResponse checkConflicts(ClassSessionCrudRequest request, UUID excludeSessionId) {
        validateTime(request);
        var classGroup = classGroupRepository.findById(request.classGroupId())
                .orElseThrow(() -> new NotFoundException("Class group not found"));
        var conflicts = excludeSessionId == null
                ? classSessionConflictService.checkForCreate(classGroup.getAcademy().getId(), request.sessionDate(), request.startTime(), request.endTime(), request.locationId(), request.coachIds())
                : classSessionConflictService.checkForUpdate(excludeSessionId, classGroup.getAcademy().getId(), request.sessionDate(), request.startTime(), request.endTime(), request.locationId(), request.coachIds());
        String message = conflicts.isEmpty() ? "No conflicts detected" : "Session conflict detected";
        return new ClassSessionConflictCheckResponse(!conflicts.isEmpty(), conflicts, message);
    }

    private ClassSession getActiveSession(UUID id) {
        return classSessionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Class session not found"));
    }

    private void validateTime(ClassSessionCrudRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BadRequestException("startTime must be before endTime");
        }
    }

    private void syncCoaches(ClassSession session, List<UUID> coachIds) {
        List<ClassSessionCoach> existing = classSessionCoachRepository.findByClassSessionIdAndDeletedAtIsNull(session.getId());
        OffsetDateTime now = OffsetDateTime.now();
        existing.forEach(it -> {
            it.setDeletedAt(now);
            it.setUpdatedAt(now);
        });
        classSessionCoachRepository.saveAll(existing);

        if (coachIds == null) return;
        List<ClassSessionCoach> inserts = new ArrayList<>();
        for (UUID coachId : coachIds) {
            if (coachId == null) continue;
            ClassSessionCoach c = new ClassSessionCoach();
            c.setClassSession(session);
            c.setAcademy(session.getClassGroup().getAcademy());
            c.setCoach(new com.example.boost.domain.entity.CoachProfile());
            c.getCoach().setId(coachId);
            inserts.add(c);
        }
        classSessionCoachRepository.saveAll(inserts);
    }

    private ClassSessionResponse toResponse(ClassSession session) {
        List<UUID> coachIds = classSessionCoachRepository.findByClassSessionIdAndDeletedAtIsNull(session.getId())
                .stream().map(c -> c.getCoach().getId()).toList();
        return new ClassSessionResponse(
                session.getId(),
                session.getClassGroup().getId(),
                session.getClassGroup().getAcademy().getId(),
                session.getSessionDate(),
                session.getStartTime(),
                session.getEndTime(),
                session.getLocationId(),
                session.getStatus(),
                coachIds,
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}
