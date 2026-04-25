package com.example.boost.iam.api;

import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.AuditLogQuery;
import com.example.boost.domain.dto.AuditLogResponse;
import com.example.boost.iam.application.AuditLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        AuditLogQuery query = AuditLogQuery.builder()
                .actorId(actorId)
                .actorUsername(actorUsername)
                .eventType(eventType)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .status(status)
                .requestId(requestId)
                .build();

        Page<AuditLogResponse> page = auditLogService.findAllResponses(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Audit logs retrieved", page));
    }
}
