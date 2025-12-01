package com.example.workorder.repository;

import com.example.workorder.domain.Access;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessRepository extends JpaRepository<Access, Long> {
}
