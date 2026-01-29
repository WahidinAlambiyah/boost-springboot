package com.example.boost.domain.mapper;

import com.example.boost.domain.dto.AuditLogResponse;
import com.example.boost.domain.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {
    public AuditLogResponse toResponse(AuditLog auditLog) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(auditLog.getId());
        response.setActorId(auditLog.getActorId());
        response.setActorUsername(auditLog.getActorUsername());
        response.setActorType(auditLog.getActorType());
        response.setAction(auditLog.getAction());
        response.setEventType(auditLog.getEventType());
        response.setEntityType(auditLog.getEntityType());
        response.setEntityId(auditLog.getEntityId());
        response.setBeforeJson(auditLog.getBeforeJson());
        response.setAfterJson(auditLog.getAfterJson());
        response.setMetadataJson(auditLog.getMetadataJson());
        response.setOccurredAt(auditLog.getOccurredAt());
        response.setIp(auditLog.getIp());
        response.setUserAgent(auditLog.getUserAgent());
        response.setRequestId(auditLog.getRequestId());
        response.setStatus(auditLog.getStatus());
        response.setReason(auditLog.getReason());
        return response;
    }
}
