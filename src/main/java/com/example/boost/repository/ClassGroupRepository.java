package com.example.boost.repository;

import com.example.boost.domain.entity.ClassGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClassGroupRepository extends JpaRepository<ClassGroup, UUID> {
}
