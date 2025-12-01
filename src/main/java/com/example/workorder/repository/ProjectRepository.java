package com.example.workorder.repository;

import com.example.workorder.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByDivisionId(Long divisionId);
}
