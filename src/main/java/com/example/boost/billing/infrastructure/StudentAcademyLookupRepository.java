package com.example.boost.billing.infrastructure;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StudentAcademyLookupRepository extends Repository<Object, UUID> {
    @Query(value = "select exists(select 1 from fastworks_springboot.students s where s.id = :studentId and s.academy_id = :academyId and s.deleted_at is null)", nativeQuery = true)
    boolean existsByStudentIdAndAcademyId(@Param("studentId") UUID studentId, @Param("academyId") UUID academyId);
}
