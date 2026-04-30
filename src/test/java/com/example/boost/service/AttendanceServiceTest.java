package com.example.boost.service;

import com.example.boost.common.exception.NotFoundException;
import com.example.boost.common.exception.UnprocessableEntityException;
import com.example.boost.domain.dto.AttendanceBatchUpsertRecordRequest;
import com.example.boost.domain.dto.AttendanceBatchUpsertRequest;
import com.example.boost.domain.dto.AttendanceUpsertRequest;
import com.example.boost.domain.entity.Academy;
import com.example.boost.domain.entity.ClassGroup;
import com.example.boost.domain.entity.ClassSession;
import com.example.boost.domain.entity.Enrollment;
import com.example.boost.domain.entity.EnrollmentStatus;
import com.example.boost.notification.application.OutboxEventService;
import com.example.boost.scheduling.application.AttendanceService;
import com.example.boost.scheduling.infrastructure.AttendanceRepository;
import com.example.boost.scheduling.infrastructure.ClassSessionRepository;
import com.example.boost.scheduling.infrastructure.EnrollmentJpaRepository;
import com.example.boost.security.CurrentActorProvider;
import com.example.boost.security.ScopeGuardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private ClassSessionRepository classSessionRepository;
    @Mock
    private EnrollmentJpaRepository enrollmentJpaRepository;
    @Mock
    private OutboxEventService outboxEventService;
    @Mock
    private CurrentActorProvider currentActorProvider;
    @Mock
    private ScopeGuardRepository scopeGuardRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void upsertRejectsWhenStudentOutsideSessionAcademy() {
        UUID classSessionId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID academyId = UUID.randomUUID();

        when(classSessionRepository.findByIdAndDeletedAtIsNull(classSessionId)).thenReturn(Optional.of(session(classSessionId, academyId)));
        when(attendanceRepository.existsActiveStudentInAcademy(studentId, academyId)).thenReturn(false);

        assertThatThrownBy(() -> attendanceService.upsertStudentAttendance(classSessionId, studentId, new AttendanceUpsertRequest("PRESENT", null, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Student not found");
    }

    @Test
    void upsertRejectsWhenStudentNotActivelyEnrolled() {
        UUID classSessionId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID academyId = UUID.randomUUID();
        UUID classGroupId = UUID.randomUUID();

        ClassSession session = session(classSessionId, academyId);
        session.getClassGroup().setId(classGroupId);
        when(classSessionRepository.findByIdAndDeletedAtIsNull(classSessionId)).thenReturn(Optional.of(session));
        when(attendanceRepository.existsActiveStudentInAcademy(studentId, academyId)).thenReturn(true);

        Enrollment waitlist = new Enrollment();
        waitlist.setStatus(EnrollmentStatus.WAITLIST);
        when(enrollmentJpaRepository.findByStudentIdAndClassGroupId(studentId, classGroupId)).thenReturn(Optional.of(waitlist));

        assertThatThrownBy(() -> attendanceService.upsertStudentAttendance(classSessionId, studentId, new AttendanceUpsertRequest("PRESENT", null, null)))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessageContaining("not actively enrolled");
    }

    @Test
    void upsertSucceedsWhenStudentInAcademyAndActiveEnrollment() {
        UUID classSessionId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID academyId = UUID.randomUUID();
        UUID classGroupId = UUID.randomUUID();

        ClassSession session = session(classSessionId, academyId);
        session.getClassGroup().setId(classGroupId);
        when(classSessionRepository.findByIdAndDeletedAtIsNull(classSessionId)).thenReturn(Optional.of(session));
        when(attendanceRepository.existsActiveStudentInAcademy(studentId, academyId)).thenReturn(true);

        Enrollment active = new Enrollment();
        active.setStatus(EnrollmentStatus.ACTIVE);
        when(enrollmentJpaRepository.findByStudentIdAndClassGroupId(studentId, classGroupId)).thenReturn(Optional.of(active));

        doNothing().when(attendanceRepository).upsertAttendance(classSessionId, studentId, new AttendanceUpsertRequest("PRESENT", null, null));

        attendanceService.upsertStudentAttendance(classSessionId, studentId, new AttendanceUpsertRequest("PRESENT", null, null));

        verify(attendanceRepository).upsertAttendance(classSessionId, studentId, new AttendanceUpsertRequest("PRESENT", null, null));
    }



    @Test
    void batchUpsertShouldProcessRecordsIncludingDuplicateStudentInSameSession() {
        UUID classSessionId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID academyId = UUID.randomUUID();
        UUID classGroupId = UUID.randomUUID();

        ClassSession session = session(classSessionId, academyId);
        session.getClassGroup().setId(classGroupId);
        when(classSessionRepository.findByIdAndDeletedAtIsNull(classSessionId)).thenReturn(Optional.of(session));
        when(attendanceRepository.existsActiveStudentInAcademy(studentId, academyId)).thenReturn(true);
        Enrollment active = new Enrollment();
        active.setStatus(EnrollmentStatus.ACTIVE);
        when(enrollmentJpaRepository.findByStudentIdAndClassGroupId(studentId, classGroupId)).thenReturn(Optional.of(active));

        AttendanceUpsertRequest first = new AttendanceUpsertRequest("PRESENT", null, "first");
        AttendanceUpsertRequest second = new AttendanceUpsertRequest("LATE", null, "update");

        attendanceService.upsertSessionAttendanceBatch(classSessionId, new AttendanceBatchUpsertRequest(java.util.List.of(
                new AttendanceBatchUpsertRecordRequest(studentId, first),
                new AttendanceBatchUpsertRecordRequest(studentId, second)
        )));

        verify(attendanceRepository).upsertAttendance(classSessionId, studentId, first);
        verify(attendanceRepository).upsertAttendance(classSessionId, studentId, second);
    }
    private ClassSession session(UUID sessionId, UUID academyId) {
        Academy academy = new Academy();
        academy.setId(academyId);

        ClassGroup classGroup = new ClassGroup();
        classGroup.setAcademy(academy);

        ClassSession classSession = new ClassSession();
        classSession.setId(sessionId);
        classSession.setClassGroup(classGroup);
        return classSession;
    }
}
