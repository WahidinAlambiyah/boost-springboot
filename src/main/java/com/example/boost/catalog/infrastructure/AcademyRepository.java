package com.example.boost.catalog.infrastructure;

import com.example.boost.common.exception.ConflictException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.entity.Academy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AcademyRepository extends JpaRepository<Academy, UUID> {

    Page<Academy> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(String codeKeyword, String nameKeyword,
            Pageable pageable);

    Optional<Academy> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByCodeIgnoreCaseAndDeletedAtIsNull(String code);

    default Academy getActiveByIdOrThrow(UUID id) {
        return findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Academy not found"));
    }

    default void ensureActiveCodeUnique(String code) {
        if (existsByCodeIgnoreCaseAndDeletedAtIsNull(code)) {
            throw new ConflictException("Academy code already exists");
        }
    }
}
