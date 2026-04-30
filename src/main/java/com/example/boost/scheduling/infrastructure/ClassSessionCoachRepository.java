package com.example.boost.scheduling.infrastructure;

import com.example.boost.domain.entity.ClassSessionCoach;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassSessionCoachRepository extends JpaRepository<ClassSessionCoach, UUID> {

    List<ClassSessionCoach> findByClassSessionIdAndDeletedAtIsNull(UUID classSessionId);

    Optional<ClassSessionCoach> findByClassSessionIdAndCoachIdAndDeletedAtIsNull(UUID classSessionId, UUID coachId);

    @Transactional
    @Modifying
    @Query("""
            update ClassSessionCoach csc
            set csc.deletedAt = :deletedAt,
                csc.updatedAt = :deletedAt
            where csc.id = :id
              and csc.deletedAt is null
            """)
    int softDeleteById(@Param("id") UUID id, @Param("deletedAt") OffsetDateTime deletedAt);

    @Query("""
            select csc from ClassSessionCoach csc
            join csc.classSession cs
            where csc.coach.id = :coachId
              and csc.deletedAt is null
              and cs.deletedAt is null
              and cs.id <> :excludedClassSessionId
              and cs.sessionDate = :sessionDate
              and cs.startTime < :endTime
              and cs.endTime > :startTime
            """)
    List<ClassSessionCoach> findCoachOverlaps(
            @Param("coachId") UUID coachId,
            @Param("sessionDate") LocalDate sessionDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludedClassSessionId") UUID excludedClassSessionId
    );
}
