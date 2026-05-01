package com.example.boost.service;

import com.example.boost.assessment.application.AssessmentRequestValidator;
import com.example.boost.assessment.application.StudentAssessmentService;
import com.example.boost.assessment.infrastructure.StudentAssessmentRepository;
import com.example.boost.assessment.infrastructure.StudentAssessmentScoreRepository;
import com.example.boost.catalog.infrastructure.AcademyRepository;
import com.example.boost.common.exception.BadRequestException;
import com.example.boost.domain.dto.AssessmentCreateRequest;
import com.example.boost.domain.dto.AssessmentScoreRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentAssessmentServiceTest {

    @Mock
    private StudentAssessmentRepository studentAssessmentRepository;
    @Mock
    private StudentAssessmentScoreRepository studentAssessmentScoreRepository;
    @Mock
    private AcademyRepository academyRepository;
    @Mock
    private AssessmentRequestValidator assessmentRequestValidator;

    private StudentAssessmentService service;

    @BeforeEach
    void setUp() {
        service = new StudentAssessmentService(
                studentAssessmentRepository,
                studentAssessmentScoreRepository,
                academyRepository,
                assessmentRequestValidator
        );
    }

    @Test
    void createShouldRejectWhenStudentAcademyMismatch() {
        UUID academyId = UUID.randomUUID();
        AssessmentCreateRequest request = request();
        when(studentAssessmentRepository.existsStudentInAcademy(request.studentId(), academyId)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> service.create(academyId, request));
    }

    @Test
    void createShouldRejectWhenCoachAcademyMismatch() {
        UUID academyId = UUID.randomUUID();
        AssessmentCreateRequest request = request();
        when(studentAssessmentRepository.existsStudentInAcademy(request.studentId(), academyId)).thenReturn(true);
        when(studentAssessmentRepository.existsCoachInAcademy(request.coachId(), academyId)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> service.create(academyId, request));
    }

    @Test
    void createShouldRejectWhenClassSessionAcademyMismatch() {
        UUID academyId = UUID.randomUUID();
        AssessmentCreateRequest request = request();
        when(studentAssessmentRepository.existsStudentInAcademy(request.studentId(), academyId)).thenReturn(true);
        when(studentAssessmentRepository.existsCoachInAcademy(request.coachId(), academyId)).thenReturn(true);
        when(studentAssessmentRepository.existsClassSessionInAcademy(request.classSessionId(), academyId)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> service.create(academyId, request));
    }

    private AssessmentCreateRequest request() {
        return new AssessmentCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                OffsetDateTime.now(),
                "notes",
                List.of(new AssessmentScoreRequest("DISCIPLINE", BigDecimal.ONE, "ok"))
        );
    }
}
