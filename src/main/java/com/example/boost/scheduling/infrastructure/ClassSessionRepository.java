package com.example.boost.scheduling.infrastructure;

import com.example.boost.domain.entity.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface ClassSessionRepository extends JpaRepository<ClassSession, UUID> {

    List<ClassSession> findAllByDeletedAtIsNull();

    java.util.Optional<ClassSession> findByIdAndDeletedAtIsNull(UUID id);


    @Query("""
            select cs from ClassSession cs
            join cs.classGroup cg
            where cg.academy.id = :academyId
              and cs.sessionDate = :sessionDate
              and cs.startTime < :endTime
              and cs.endTime > :startTime
              and (:excludedSessionId is null or cs.id <> :excludedSessionId)
              and upper(coalesce(cs.status, 'SCHEDULED')) <> 'CANCELLED'
            """)
    List<ClassSession> findOverlappingSessions(
            @Param("academyId") UUID academyId,
            @Param("sessionDate") LocalDate sessionDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludedSessionId") UUID excludedSessionId
    );
}
