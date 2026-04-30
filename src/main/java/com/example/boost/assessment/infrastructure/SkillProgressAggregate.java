package com.example.boost.assessment.infrastructure;

import java.math.BigDecimal;
import java.util.UUID;

public interface SkillProgressAggregate {
    UUID getSkillId();
    String getSkillCode();
    String getSkillName();
    Long getAssessmentCount();
    BigDecimal getAverageScore();
}
