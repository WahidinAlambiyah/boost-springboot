package com.example.boost.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class AuditLogResponse {
    private UUID id;
    private UUID actorId;
    private String actorUsername;
    private String actorType;
    private String action;
    private String eventType;
    private String entityType;
    private String entityId;
    private String beforeJson;
    private String afterJson;
    private String metadataJson;
    private OffsetDateTime occurredAt;
    private String ip;
    private String userAgent;
    private UUID requestId;
    private String status;
    private String reason;
}
