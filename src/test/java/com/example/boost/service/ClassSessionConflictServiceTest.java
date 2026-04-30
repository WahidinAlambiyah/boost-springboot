package com.example.boost.service;

import com.example.boost.domain.entity.Academy;
import com.example.boost.domain.entity.ClassGroup;
import com.example.boost.domain.entity.ClassSession;
import com.example.boost.domain.entity.ClassSessionCoach;
import com.example.boost.scheduling.application.ClassSessionConflictService;
import com.example.boost.scheduling.infrastructure.ClassSessionCoachRepository;
import com.example.boost.scheduling.infrastructure.ClassSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassSessionConflictServiceTest {

    @Mock
    private ClassSessionRepository classSessionRepository;
    @Mock
    private ClassSessionCoachRepository classSessionCoachRepository;

    @InjectMocks
    private ClassSessionConflictService conflictService;

    @Test
    void checkForCreateReturnsTimeOverlapConflictForAcademyOverlap() {
        UUID academyId = UUID.randomUUID();
        UUID overlappingId = UUID.randomUUID();

        when(classSessionRepository.findOverlappingSessions(academyId, LocalDate.of(2026, 5, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), null))
                .thenReturn(List.of(session(overlappingId, UUID.randomUUID(), "SCHEDULED")));

        var conflicts = conflictService.checkForCreate(
                academyId,
                LocalDate.of(2026, 5, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                UUID.randomUUID(),
                List.of()
        );

        assertThat(conflicts)
                .hasSize(1)
                .allSatisfy(conflict -> {
                    assertThat(conflict.getType()).isEqualTo(ClassSessionConflictService.ConflictType.TIME_OVERLAP);
                    assertThat(conflict.getConflictingSessionId()).isEqualTo(overlappingId);
                });
    }

    @Test
    void checkForCreateReturnsLocationOverlapConflictForLocationOverlap() {
        UUID academyId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID overlappingId = UUID.randomUUID();

        when(classSessionRepository.findOverlappingSessions(academyId, LocalDate.of(2026, 5, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), null))
                .thenReturn(List.of(session(overlappingId, locationId, "SCHEDULED")));

        var conflicts = conflictService.checkForCreate(
                academyId,
                LocalDate.of(2026, 5, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                locationId,
                List.of()
        );

        assertThat(conflicts)
                .extracting(ClassSessionConflictService.SessionConflict::getType)
                .containsExactlyInAnyOrder(
                        ClassSessionConflictService.ConflictType.TIME_OVERLAP,
                        ClassSessionConflictService.ConflictType.LOCATION_OVERLAP
                );
    }

    @Test
    void checkForCreateReturnsCoachOverlapConflictForCoachOverlap() {
        UUID academyId = UUID.randomUUID();
        UUID coachId = UUID.randomUUID();
        UUID overlappingId = UUID.randomUUID();

        when(classSessionRepository.findOverlappingSessions(academyId, LocalDate.of(2026, 5, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), null))
                .thenReturn(List.of());
        when(classSessionCoachRepository.findCoachOverlaps(academyId, coachId, LocalDate.of(2026, 5, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), null))
                .thenReturn(List.of(coachAssignment(overlappingId)));

        var conflicts = conflictService.checkForCreate(
                academyId,
                LocalDate.of(2026, 5, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                UUID.randomUUID(),
                List.of(coachId)
        );

        assertThat(conflicts)
                .hasSize(1)
                .allSatisfy(conflict -> {
                    assertThat(conflict.getType()).isEqualTo(ClassSessionConflictService.ConflictType.COACH_OVERLAP);
                    assertThat(conflict.getCoachId()).isEqualTo(coachId);
                    assertThat(conflict.getConflictingSessionId()).isEqualTo(overlappingId);
                });
    }

    @Test
    void checkForCreateReturnsNoConflictWhenSessionsDoNotOverlap() {
        UUID academyId = UUID.randomUUID();
        UUID coachId = UUID.randomUUID();

        when(classSessionRepository.findOverlappingSessions(academyId, LocalDate.of(2026, 5, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), null))
                .thenReturn(List.of());
        when(classSessionCoachRepository.findCoachOverlaps(academyId, coachId, LocalDate.of(2026, 5, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), null))
                .thenReturn(List.of());

        var conflicts = conflictService.checkForCreate(
                academyId,
                LocalDate.of(2026, 5, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                UUID.randomUUID(),
                List.of(coachId)
        );

        assertThat(conflicts).isEmpty();
    }

    @Test
    void checkForCreateIgnoresCancelledSession() {
        UUID academyId = UUID.randomUUID();

        when(classSessionRepository.findOverlappingSessions(academyId, LocalDate.of(2026, 5, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), null))
                .thenReturn(List.of(session(UUID.randomUUID(), UUID.randomUUID(), "CANCELLED")));

        var conflicts = conflictService.checkForCreate(
                academyId,
                LocalDate.of(2026, 5, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                UUID.randomUUID(),
                List.of()
        );

        assertThat(conflicts).isEmpty();
    }

    @Test
    void checkForUpdateExcludesCurrentSessionFromConflictDetection() {
        UUID academyId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID coachId = UUID.randomUUID();

        when(classSessionRepository.findOverlappingSessions(academyId, LocalDate.of(2026, 5, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), sessionId))
                .thenReturn(List.of());
        when(classSessionCoachRepository.findCoachOverlaps(academyId, coachId, LocalDate.of(2026, 5, 1), LocalTime.of(10, 0), LocalTime.of(11, 0), sessionId))
                .thenReturn(List.of());

        var conflicts = conflictService.checkForUpdate(
                sessionId,
                academyId,
                LocalDate.of(2026, 5, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                UUID.randomUUID(),
                List.of(coachId)
        );

        assertThat(conflicts).isEmpty();
    }

    private ClassSession session(UUID sessionId, UUID locationId, String status) {
        Academy academy = new Academy();
        academy.setId(UUID.randomUUID());

        ClassGroup classGroup = new ClassGroup();
        classGroup.setAcademy(academy);

        ClassSession classSession = new ClassSession();
        classSession.setId(sessionId);
        classSession.setClassGroup(classGroup);
        classSession.setLocationId(locationId);
        classSession.setStatus(status);
        return classSession;
    }

    private ClassSessionCoach coachAssignment(UUID sessionId) {
        ClassSession classSession = new ClassSession();
        classSession.setId(sessionId);

        ClassSessionCoach assignment = new ClassSessionCoach();
        assignment.setClassSession(classSession);
        return assignment;
    }
}
