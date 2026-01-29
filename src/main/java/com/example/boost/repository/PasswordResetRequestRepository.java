package com.example.boost.repository;

import com.example.boost.domain.entity.PasswordResetRequest;
import com.example.boost.domain.entity.PasswordResetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, UUID> {
    Optional<PasswordResetRequest> findFirstByEmailAndStatusOrderByCreatedAtDesc(String email, PasswordResetStatus status);

    Optional<PasswordResetRequest> findByTokenHashAndStatus(String tokenHash, PasswordResetStatus status);
}
