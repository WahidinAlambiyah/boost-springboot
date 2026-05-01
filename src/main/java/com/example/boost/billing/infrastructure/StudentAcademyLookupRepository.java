package com.example.boost.billing.infrastructure;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StudentAcademyLookupRepository {
    private final EntityManager entityManager;

    public boolean existsByStudentIdAndAcademyId(UUID studentId, UUID academyId) {
        Boolean exists = (Boolean) entityManager
                .createNativeQuery("""
                        select exists(
                            select 1
                            from fastworks_springboot.students s
                            where s.id = :studentId
                              and s.academy_id = :academyId
                              and s.deleted_at is null
                        )
                        """)
                .setParameter("studentId", studentId)
                .setParameter("academyId", academyId)
                .getSingleResult();
        return Boolean.TRUE.equals(exists);
    }
}
