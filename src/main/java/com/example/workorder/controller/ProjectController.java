package com.example.workorder.controller;

import com.example.workorder.dto.ProjectRequest;
import com.example.workorder.dto.ProjectResponse;
import com.example.workorder.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final RequestContextResolver contextResolver;

    public ProjectController(ProjectService projectService, RequestContextResolver contextResolver) {
        this.projectService = projectService;
        this.contextResolver = contextResolver;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@RequestBody @Valid ProjectRequest request,
                                                  @RequestHeader HttpHeaders headers) {
        RequestContext context = contextResolver.resolve(headers);
        return ResponseEntity.ok(projectService.create(request, context.getRole(), context.getDivisionId()));
    }

    @GetMapping("/division/{divisionId}")
    public ResponseEntity<List<ProjectResponse>> findByDivision(@PathVariable Long divisionId,
                                                                 @RequestHeader HttpHeaders headers) {
        RequestContext context = contextResolver.resolve(headers);
        return ResponseEntity.ok(projectService.findByDivision(divisionId, context.getRole(), context.getDivisionId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> findOne(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        RequestContext context = contextResolver.resolve(headers);
        return ResponseEntity.ok(projectService.findOne(id, context.getRole(), context.getDivisionId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable Long id, @RequestBody @Valid ProjectRequest request,
                                                  @RequestHeader HttpHeaders headers) {
        RequestContext context = contextResolver.resolve(headers);
        return ResponseEntity.ok(projectService.update(id, request, context.getRole(), context.getDivisionId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        RequestContext context = contextResolver.resolve(headers);
        projectService.delete(id, context.getRole(), context.getDivisionId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<ProjectResponse> resume(@PathVariable Long id, @RequestHeader HttpHeaders headers) {
        RequestContext context = contextResolver.resolve(headers);
        return ResponseEntity.ok(projectService.resume(id, context.getRole(), context.getDivisionId()));
    }
}
