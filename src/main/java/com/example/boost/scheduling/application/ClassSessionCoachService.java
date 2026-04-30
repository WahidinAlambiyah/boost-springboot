package com.example.boost.scheduling.application;

import com.example.boost.catalog.infrastructure.CoachProfileRepository;
import com.example.boost.common.exception.BadRequestException;
import com.example.boost.common.exception.ConflictException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.dto.ClassSessionCoachResponse;
import com.example.boost.domain.dto.ClassSessionCoachUpsertRequest;
import com.example.boost.domain.entity.ClassSessionCoach;
import com.example.boost.scheduling.infrastructure.ClassSessionCoachRepository;
import com.example.boost.scheduling.infrastructure.ClassSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassSessionCoachService {

    private final ClassSessionRepository classSessionRepository;
    private final ClassSessionCoachRepository classSessionCoachRepository;
    private final CoachProfileRepository coachProfileRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('SCHEDULE_READ','SESSION_READ')")
    public List<ClassSessionCoachResponse> list(UUID classSessionId) {
        return classSessionCoachRepository.findByClassSessionIdAndDeletedAtIsNull(classSessionId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('SCHEDULE_WRITE','SESSION_WRITE')")
    public ClassSessionCoachResponse create(UUID classSessionId, ClassSessionCoachUpsertRequest request) {
        var session = classSessionRepository.findByIdAndDeletedAtIsNull(classSessionId)
                .orElseThrow(() -> new NotFoundException("Class session not found"));
        var coach = coachProfileRepository.getActiveByIdAndAcademyIdOrThrow(request.coachId(), session.getClassGroup().getAcademy().getId());

        classSessionCoachRepository.findByClassSessionIdAndCoachIdAndDeletedAtIsNull(classSessionId, request.coachId())
                .ifPresent(existing -> {
                    throw new ConflictException("Coach already assigned to this session");
                });

        validateCoachOverlap(session, request.coachId(), null);

        ClassSessionCoach assignment = new ClassSessionCoach();
        assignment.setClassSession(session);
        assignment.setAcademy(session.getClassGroup().getAcademy());
        assignment.setCoach(coach);
        assignment.setCoachRole(request.coachRole());
        assignment.setAttendanceStatus(request.attendanceStatus());
        assignment.setCheckInAt(request.checkInAt());
        assignment.setCheckOutAt(request.checkOutAt());
        assignment.setNotes(request.notes());
        classSessionCoachRepository.save(assignment);
        return toResponse(assignment);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('SCHEDULE_WRITE','SESSION_WRITE')")
    public ClassSessionCoachResponse update(UUID classSessionId, UUID sessionCoachId, ClassSessionCoachUpsertRequest request) {
        var session = classSessionRepository.findByIdAndDeletedAtIsNull(classSessionId)
                .orElseThrow(() -> new NotFoundException("Class session not found"));
        var assignment = classSessionCoachRepository.findById(sessionCoachId)
                .filter(c -> c.getDeletedAt() == null)
                .filter(c -> c.getClassSession().getId().equals(classSessionId))
                .orElseThrow(() -> new NotFoundException("Session coach assignment not found"));

        var coach = coachProfileRepository.getActiveByIdAndAcademyIdOrThrow(request.coachId(), session.getClassGroup().getAcademy().getId());

        classSessionCoachRepository.findByClassSessionIdAndCoachIdAndDeletedAtIsNull(classSessionId, request.coachId())
                .filter(existing -> !existing.getId().equals(sessionCoachId))
                .ifPresent(existing -> {
                    throw new ConflictException("Coach already assigned to this session");
                });

        validateCoachOverlap(session, request.coachId(), session.getId());

        assignment.setCoach(coach);
        assignment.setCoachRole(request.coachRole());
        assignment.setAttendanceStatus(request.attendanceStatus());
        assignment.setCheckInAt(request.checkInAt());
        assignment.setCheckOutAt(request.checkOutAt());
        assignment.setNotes(request.notes());
        classSessionCoachRepository.save(assignment);
        return toResponse(assignment);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('SCHEDULE_WRITE','SESSION_WRITE')")
    public void softDelete(UUID classSessionId, UUID sessionCoachId) {
        var assignment = classSessionCoachRepository.findById(sessionCoachId)
                .filter(c -> c.getDeletedAt() == null)
                .filter(c -> c.getClassSession().getId().equals(classSessionId))
                .orElseThrow(() -> new NotFoundException("Session coach assignment not found"));
        OffsetDateTime now = OffsetDateTime.now();
        assignment.setDeletedAt(now);
        assignment.setUpdatedAt(now);
        classSessionCoachRepository.save(assignment);
    }

    private void validateCoachOverlap(com.example.boost.domain.entity.ClassSession session, UUID coachId, UUID excludedSessionId) {
        var overlaps = classSessionCoachRepository.findCoachOverlaps(
                session.getClassGroup().getAcademy().getId(),
                coachId,
                session.getSessionDate(),
                session.getStartTime(),
                session.getEndTime(),
                excludedSessionId
        );
        if (!overlaps.isEmpty()) {
            throw new BadRequestException("Coach assignment overlaps with another class session");
        }
    }

    private ClassSessionCoachResponse toResponse(ClassSessionCoach assignment) {
        return new ClassSessionCoachResponse(
                assignment.getId(),
                assignment.getClassSession().getId(),
                assignment.getAcademy().getId(),
                assignment.getCoach().getId(),
                assignment.getCoachRole(),
                assignment.getAttendanceStatus(),
                assignment.getCheckInAt(),
                assignment.getCheckOutAt(),
                assignment.getNotes(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt()
        );
    }
}
