package com.example.boost.domain.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class AuditLogQuery {
    UUID actorId;
    String actorUsername;
    String eventType;
    String action;
    String entityType;
    String entityId;
    String status;
    UUID requestId;
}
