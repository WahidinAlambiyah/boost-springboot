package com.example.boost.billing.infrastructure;

import com.example.boost.domain.entity.CoachPayrollItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CoachPayrollItemRepository extends JpaRepository<CoachPayrollItem, UUID> {

    List<CoachPayrollItem> findByPayrollPeriodIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID payrollPeriodId);

    @Query(value = """
            select csc.coach_id as coachId,
                   count(*) as totalSessions,
                   sum(case when lower(coalesce(csc.attendance_status, '')) = 'present' then 1 else 0 end) as presentSessions,
                   sum(case when lower(coalesce(csc.attendance_status, '')) = 'absent' then 1 else 0 end) as absentSessions
            from fastworks_springboot.class_session_coaches csc
            join fastworks_springboot.class_sessions cs on cs.id = csc.class_session_id
            where csc.academy_id = :academyId
              and csc.deleted_at is null
              and cs.deleted_at is null
              and extract(month from cs.session_date) = :month
              and extract(year from cs.session_date) = :year
            group by csc.coach_id
            """, nativeQuery = true)
    List<CoachMonthlySessionAggregate> aggregateCoachMonthlySessions(
            @Param("academyId") UUID academyId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    interface CoachMonthlySessionAggregate {
        UUID getCoachId();

        Long getTotalSessions();

        Long getPresentSessions();

        Long getAbsentSessions();
    }
}
