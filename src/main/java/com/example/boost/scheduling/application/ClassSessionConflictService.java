package com.example.boost.scheduling.application;

import com.example.boost.domain.entity.ClassSession;
import com.example.boost.scheduling.infrastructure.ClassSessionCoachRepository;
import com.example.boost.scheduling.infrastructure.ClassSessionRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassSessionConflictService {

    private final ClassSessionRepository classSessionRepository;
    private final ClassSessionCoachRepository classSessionCoachRepository;

    @Transactional(readOnly = true)
    public List<SessionConflict> checkForCreate(
            UUID academyId,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            UUID locationId,
            Collection<UUID> coachIds
    ) {
        return findConflicts(null, academyId, sessionDate, startTime, endTime, locationId, coachIds);
    }

    @Transactional(readOnly = true)
    public List<SessionConflict> checkForUpdate(
            UUID sessionId,
            UUID academyId,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            UUID locationId,
            Collection<UUID> coachIds
    ) {
        return findConflicts(sessionId, academyId, sessionDate, startTime, endTime, locationId, coachIds);
    }

    @Transactional(readOnly = true)
    public List<SessionConflict> findConflicts(
            UUID excludedSessionId,
            UUID academyId,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            UUID locationId,
            Collection<UUID> coachIds
    ) {
        List<ClassSession> overlappingSessions = classSessionRepository.findOverlappingSessions(
                academyId,
                sessionDate,
                startTime,
                endTime,
                excludedSessionId
        );

        List<SessionConflict> conflicts = new ArrayList<>();

        for (ClassSession overlapping : overlappingSessions) {
            if ("CANCELLED".equalsIgnoreCase(overlapping.getStatus())) {
                continue;
            }
            conflicts.add(SessionConflict.builder()
                    .type(ConflictType.TIME_OVERLAP)
                    .conflictingSessionId(overlapping.getId())
                    .message("Overlapping session exists in the same academy and date")
                    .build());

            if (locationId != null && locationId.equals(overlapping.getLocationId())) {
                conflicts.add(SessionConflict.builder()
                        .type(ConflictType.LOCATION_OVERLAP)
                        .conflictingSessionId(overlapping.getId())
                        .locationId(locationId)
                        .message("Location is used by another overlapping session")
                        .build());
            }
        }

        if (coachIds != null) {
            for (UUID coachId : coachIds) {
                if (coachId == null) {
                    continue;
                }
                var coachOverlaps = classSessionCoachRepository.findCoachOverlaps(
                        academyId,
                        coachId,
                        sessionDate,
                        startTime,
                        endTime,
                        excludedSessionId
                );

                coachOverlaps.forEach(overlap -> conflicts.add(SessionConflict.builder()
                        .type(ConflictType.COACH_OVERLAP)
                        .coachId(coachId)
                        .conflictingSessionId(overlap.getClassSession().getId())
                        .message("Coach is already assigned to another overlapping session")
                        .build()));
            }
        }

        return conflicts;
    }

    public enum ConflictType {
        TIME_OVERLAP,
        LOCATION_OVERLAP,
        COACH_OVERLAP
    }

    @Value
    @Builder
    public static class SessionConflict {
        ConflictType type;
        UUID conflictingSessionId;
        UUID locationId;
        UUID coachId;
        String message;
    }
}
