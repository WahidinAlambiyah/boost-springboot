package com.example.boost.catalog.infrastructure;

import com.example.boost.domain.entity.ClassGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClassGroupRepository extends JpaRepository<ClassGroup, UUID> {
}
