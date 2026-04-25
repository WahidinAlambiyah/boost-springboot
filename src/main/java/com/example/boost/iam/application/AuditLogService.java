package com.example.boost.iam.application;

import com.example.boost.context.RequestContext;
import com.example.boost.context.RequestContextData;
import com.example.boost.domain.dto.AuditLogQuery;
import com.example.boost.domain.dto.AuditLogResponse;
import com.example.boost.domain.entity.AuditLog;
import com.example.boost.domain.mapper.AuditLogMapper;
import com.example.boost.iam.infrastructure.AuditLogRepository;
import com.example.boost.iam.infrastructure.AuditLogSpecifications;
import com.example.boost.security.UserPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password",
            "password_hash",
            "token",
            "access_token",
            "refresh_token",
            "secret",
            "api_key",
            "authorization",
            "jwt",
            "credentials"
    );

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final AuditLogMapper auditLogMapper;

    public AuditLogBuilder securityEvent(String action) {
        return new AuditLogBuilder("SECURITY", action);
    }

    public AuditLogBuilder rbacEvent(String action) {
        return new AuditLogBuilder("RBAC", action);
    }

    public AuditLogBuilder dataEvent(String action) {
        return new AuditLogBuilder("DATA", action);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findAllResponses(AuditLogQuery query, Pageable pageable) {
        return auditLogRepository.findAll(AuditLogSpecifications.fromQuery(query), pageable)
                .map(auditLogMapper::toResponse);
    }

    public class AuditLogBuilder {
        private final String eventType;
        private final String action;
        private UUID actorId;
        private String actorUsername;
        private String actorType;
        private String entityType;
        private String entityId;
        private Object before;
        private Object after;
        private Object metadata;
        private String status;
        private String reason;

        private AuditLogBuilder(String eventType, String action) {
            this.eventType = eventType;
            this.action = action;
        }

        public AuditLogBuilder actor(UUID actorId, String actorUsername, String actorType) {
            this.actorId = actorId;
            this.actorUsername = actorUsername;
            this.actorType = actorType;
            return this;
        }

        public AuditLogBuilder entity(String entityType, String entityId) {
            this.entityType = entityType;
            this.entityId = entityId;
            return this;
        }

        public AuditLogBuilder before(Object before) {
            this.before = before;
            return this;
        }

        public AuditLogBuilder after(Object after) {
            this.after = after;
            return this;
        }

        public AuditLogBuilder metadata(Object metadata) {
            this.metadata = metadata;
            return this;
        }

        public AuditLogBuilder statusSuccess() {
            this.status = "SUCCESS";
            return this;
        }

        public AuditLogBuilder statusFailure(String reason) {
            this.status = "FAIL";
            this.reason = reason;
            return this;
        }

        public AuditLog save() {
            AuditLog auditLog = new AuditLog();
            populateActorIfMissing();
            auditLog.setActorId(actorId);
            auditLog.setActorUsername(actorUsername);
            auditLog.setActorType(actorType);
            auditLog.setAction(action);
            auditLog.setEventType(eventType);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setBeforeJson(toJson(before));
            auditLog.setAfterJson(toJson(after));
            auditLog.setMetadataJson(toJson(metadata));
            auditLog.setStatus(status);
            auditLog.setReason(reason);
            auditLog.setOccurredAt(OffsetDateTime.now());

            RequestContextData contextData = RequestContext.get();
            if (contextData != null) {
                auditLog.setIp(contextData.getIp());
                auditLog.setUserAgent(contextData.getUserAgent());
                if (contextData.getRequestId() != null) {
                    auditLog.setRequestId(UUID.fromString(contextData.getRequestId()));
                }
            }

            return auditLogRepository.save(auditLog);
        }

        private void populateActorIfMissing() {
            if (actorId != null || actorUsername != null) {
                if (actorType == null) {
                    actorType = "USER";
                }
                return;
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
                actorId = principal.getUser().getId();
                actorUsername = principal.getUsername();
                actorType = "USER";
            } else {
                actorType = "SYSTEM";
            }
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            JsonNode node = objectMapper.valueToTree(value);
            sanitize(node);
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private void sanitize(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey().toLowerCase(Locale.ROOT);
                JsonNode child = entry.getValue();
                if (SENSITIVE_FIELDS.contains(fieldName)) {
                    ((com.fasterxml.jackson.databind.node.ObjectNode) node).put(entry.getKey(), "[REDACTED]");
                } else {
                    sanitize(child);
                }
            });
        } else if (node.isArray()) {
            node.forEach(this::sanitize);
        }
    }
}
