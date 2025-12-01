package com.example.workorder.service;

import com.example.workorder.domain.Division;
import com.example.workorder.domain.Project;
import com.example.workorder.domain.ProjectStatus;
import com.example.workorder.dto.ProjectRequest;
import com.example.workorder.dto.ProjectResponse;
import com.example.workorder.exception.NotFoundException;
import com.example.workorder.repository.ProjectRepository;
import com.example.workorder.security.AccessControlService;
import com.example.workorder.security.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final DivisionService divisionService;
    private final AccessControlService accessControlService;

    public ProjectService(ProjectRepository projectRepository, DivisionService divisionService,
                          AccessControlService accessControlService) {
        this.projectRepository = projectRepository;
        this.divisionService = divisionService;
        this.accessControlService = accessControlService;
    }

    public ProjectResponse create(ProjectRequest request, UserRole role, Optional<Long> requesterDivisionId) {
        Division division = divisionService.getById(request.getDivisionId());
        accessControlService.verifyDivisionAccess(role, requesterDivisionId, division.getId());

        Project project = new Project();
        project.setDivision(division);
        applyRequest(project, request);

        Project saved = projectRepository.save(project);
        return mapToResponse(saved);
    }

    public List<ProjectResponse> findByDivision(Long divisionId, UserRole role, Optional<Long> requesterDivisionId) {
        accessControlService.verifyDivisionAccess(role, requesterDivisionId, divisionId);
        return projectRepository.findByDivisionId(divisionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse findOne(Long id, UserRole role, Optional<Long> requesterDivisionId) {
        Project project = getProject(id);
        accessControlService.verifyDivisionAccess(role, requesterDivisionId, project.getDivision().getId());
        return mapToResponse(project);
    }

    public ProjectResponse update(Long id, ProjectRequest request, UserRole role, Optional<Long> requesterDivisionId) {
        Project project = getProject(id);
        accessControlService.verifyDivisionAccess(role, requesterDivisionId, project.getDivision().getId());

        if (!project.getDivision().getId().equals(request.getDivisionId())) {
            Division newDivision = divisionService.getById(request.getDivisionId());
            accessControlService.verifyDivisionAccess(role, requesterDivisionId, newDivision.getId());
            project.setDivision(newDivision);
        }

        applyRequest(project, request);
        return mapToResponse(project);
    }

    public void delete(Long id, UserRole role, Optional<Long> requesterDivisionId) {
        Project project = getProject(id);
        accessControlService.verifyDivisionAccess(role, requesterDivisionId, project.getDivision().getId());
        projectRepository.delete(project);
    }

    public ProjectResponse resume(Long id, UserRole role, Optional<Long> requesterDivisionId) {
        Project project = getProject(id);
        accessControlService.verifyDivisionAccess(role, requesterDivisionId, project.getDivision().getId());
        if (project.getStatus() == ProjectStatus.CLOSED) {
            project.setStatus(ProjectStatus.OPEN);
        }
        return mapToResponse(project);
    }

    private void applyRequest(Project project, ProjectRequest request) {
        project.setName(request.getName());
        project.setManager(request.getManager());
        project.setRoom(request.getRoom());
        project.setStatus(request.getStatus());
        project.setStartTime(request.getStartTime());
        project.setEndTime(request.getEndTime());
    }

    private Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found: " + id));
    }

    private ProjectResponse mapToResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getDivision().getId(),
                project.getDivision().getName(),
                project.getName(),
                project.getManager(),
                project.getRoom(),
                project.getStatus(),
                project.getStartTime(),
                project.getEndTime()
        );
    }
}
