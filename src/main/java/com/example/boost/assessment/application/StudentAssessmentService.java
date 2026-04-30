package com.example.boost.assessment.application;

import com.example.boost.assessment.infrastructure.StudentAssessmentRepository;
import com.example.boost.assessment.infrastructure.StudentAssessmentScoreRepository;
import com.example.boost.catalog.infrastructure.AcademyRepository;
import com.example.boost.common.exception.BadRequestException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.dto.AssessmentCreateRequest;
import com.example.boost.domain.dto.AssessmentDetailResponse;
import com.example.boost.domain.dto.AssessmentScoreRequest;
import com.example.boost.domain.dto.AssessmentUpdateRequest;
import com.example.boost.domain.entity.AssessmentSkill;
import com.example.boost.domain.entity.StudentAssessment;
import com.example.boost.domain.entity.StudentAssessmentScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentAssessmentService {
    private final StudentAssessmentRepository studentAssessmentRepository;
    private final StudentAssessmentScoreRepository studentAssessmentScoreRepository;
    private final AcademyRepository academyRepository;
    private final AssessmentRequestValidator assessmentRequestValidator;

    @Transactional
    public AssessmentDetailResponse create(UUID academyId, AssessmentCreateRequest request) {
        validateReferences(academyId, request.classSessionId(), request.studentId(), request.coachId());
        Map<String, AssessmentSkill> skillsByCode = assessmentRequestValidator.validateAndResolveScores(request.scores(), academyId);

        StudentAssessment assessment = new StudentAssessment();
        assessment.setAcademy(academyRepository.findById(academyId).orElseThrow(() -> new NotFoundException("Academy not found")));
        assessment.setClassSessionId(request.classSessionId());
        assessment.setStudentId(request.studentId());
        assessment.setCoachId(request.coachId());
        assessment.setAssessedAt(request.assessedAt());
        assessment.setNotes(request.notes());
        StudentAssessment saved = studentAssessmentRepository.save(assessment);

        upsertScores(saved, request.scores(), skillsByCode);
        return toDetail(studentAssessmentRepository.findWithScoresById(saved.getId()).orElseThrow(() -> new NotFoundException("Assessment not found")));
    }

    @Transactional
    public AssessmentDetailResponse update(UUID id, UUID academyId, AssessmentUpdateRequest request) {
        StudentAssessment assessment = studentAssessmentRepository.findWithScoresById(id)
                .orElseThrow(() -> new NotFoundException("Assessment not found"));
        if (!assessment.getAcademy().getId().equals(academyId)) {
            throw new BadRequestException("assessment does not belong to academy");
        }
        validateReferences(academyId, request.classSessionId(), request.studentId(), request.coachId());
        Map<String, AssessmentSkill> skillsByCode = assessmentRequestValidator.validateAndResolveScores(request.scores(), academyId);

        assessment.setClassSessionId(request.classSessionId());
        assessment.setStudentId(request.studentId());
        assessment.setCoachId(request.coachId());
        assessment.setAssessedAt(request.assessedAt());
        assessment.setNotes(request.notes());
        StudentAssessment saved = studentAssessmentRepository.save(assessment);

        studentAssessmentScoreRepository.deleteAll(saved.getScores());
        upsertScores(saved, request.scores(), skillsByCode);
        return toDetail(studentAssessmentRepository.findWithScoresById(saved.getId()).orElseThrow(() -> new NotFoundException("Assessment not found")));
    }

    @Transactional
    public void softDelete(UUID id) {
        StudentAssessment assessment = studentAssessmentRepository.findWithScoresById(id)
                .orElseThrow(() -> new NotFoundException("Assessment not found"));
        java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
        assessment.setDeletedAt(now);
        for (StudentAssessmentScore score : assessment.getScores()) {
            score.setDeletedAt(now);
        }
        studentAssessmentRepository.save(assessment);
        studentAssessmentScoreRepository.saveAll(assessment.getScores());
    }

    private void validateReferences(UUID academyId, UUID classSessionId, UUID studentId, UUID coachId) {
        if (!studentAssessmentRepository.existsStudentInAcademy(studentId, academyId)) {
            throw new BadRequestException("student not found in academy");
        }
        if (!studentAssessmentRepository.existsCoachInAcademy(coachId, academyId)) {
            throw new BadRequestException("coach not found in academy");
        }
        if (!studentAssessmentRepository.existsClassSessionInAcademy(classSessionId, academyId)) {
            throw new BadRequestException("class_session not found in academy");
        }
    }

    private void upsertScores(StudentAssessment assessment, List<AssessmentScoreRequest> scoreRequests, Map<String, AssessmentSkill> skillsByCode) {
        List<StudentAssessmentScore> scores = new ArrayList<>();
        for (AssessmentScoreRequest scoreRequest : scoreRequests) {
            StudentAssessmentScore score = new StudentAssessmentScore();
            score.setAssessment(assessment);
            score.setSkill(skillsByCode.get(scoreRequest.skillCode().trim().toLowerCase(java.util.Locale.ROOT)));
            score.setScore(scoreRequest.score());
            score.setNotes(scoreRequest.notes());
            scores.add(score);
        }
        studentAssessmentScoreRepository.saveAll(scores);
    }

    private AssessmentDetailResponse toDetail(StudentAssessment assessment) {
        List<AssessmentDetailResponse.AssessmentScoreDetailResponse> scores = assessment.getScores().stream().map(score ->
                new AssessmentDetailResponse.AssessmentScoreDetailResponse(
                        score.getSkill().getId(),
                        score.getSkill().getCode(),
                        score.getSkill().getName(),
                        score.getSkill().getMaxScore(),
                        score.getScore().toPlainString(),
                        score.getNotes()
                )).toList();
        return new AssessmentDetailResponse(
                assessment.getId(),
                assessment.getClassSessionId(),
                assessment.getStudentId(),
                assessment.getCoachId(),
                assessment.getAssessedAt(),
                assessment.getNotes(),
                scores
        );
    }
}
