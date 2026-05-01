package com.example.boost.billing.infrastructure;

import com.example.boost.domain.entity.CoachPayrollPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CoachPayrollPeriodRepository extends JpaRepository<CoachPayrollPeriod, UUID> {
    Optional<CoachPayrollPeriod> findByAcademyIdAndPeriodMonthAndPeriodYearAndDeletedAtIsNull(
            UUID academyId,
            Integer periodMonth,
            Integer periodYear
    );
}
