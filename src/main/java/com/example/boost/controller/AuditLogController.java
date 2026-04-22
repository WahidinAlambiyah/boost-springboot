package com.example.boost.controller;

import com.example.boost.domain.dto.ApiResponse;
import com.example.boost.domain.dto.AuditLogResponse;
import com.example.boost.domain.entity.AuditLog;
import com.example.boost.service.AuditLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "6. Audit Log")
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_READ')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) String actorUsername,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID requestId,
            Pageable pageable) {
        Specification<AuditLog> specification = Specification.<AuditLog>where((root, query, cb) -> cb.conjunction())
                .and(actorId == null ? null : (root, query, cb) -> cb.equal(root.get("actorId"), actorId))
                .and(actorUsername == null ? null : (root, query, cb) -> cb.equal(root.get("actorUsername"), actorUsername))
                .and(eventType == null ? null : (root, query, cb) -> cb.equal(root.get("eventType"), eventType))
                .and(action == null ? null : (root, query, cb) -> cb.equal(root.get("action"), action))
                .and(entityType == null ? null : (root, query, cb) -> cb.equal(root.get("entityType"), entityType))
                .and(entityId == null ? null : (root, query, cb) -> cb.equal(root.get("entityId"), entityId))
                .and(status == null ? null : (root, query, cb) -> cb.equal(root.get("status"), status))
                .and(requestId == null ? null : (root, query, cb) -> cb.equal(root.get("requestId"), requestId));

        Page<AuditLogResponse> page = auditLogService.findAllResponses(specification, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Audit logs retrieved", page));
    }
}
