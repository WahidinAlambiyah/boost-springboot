package com.example.boost.billing.infrastructure;

import com.example.boost.domain.entity.StudentPackageSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudentPackageSubscriptionRepository extends JpaRepository<StudentPackageSubscription, UUID> {
    List<StudentPackageSubscription> findByStudentIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID studentId);
}
