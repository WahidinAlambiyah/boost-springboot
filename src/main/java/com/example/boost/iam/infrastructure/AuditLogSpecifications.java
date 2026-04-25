package com.example.boost.iam.infrastructure;

import com.example.boost.domain.dto.AuditLogQuery;
import com.example.boost.domain.entity.AuditLog;
import org.springframework.data.jpa.domain.Specification;

public final class AuditLogSpecifications {

    private AuditLogSpecifications() {
    }

    public static Specification<AuditLog> fromQuery(AuditLogQuery query) {
        return Specification.<AuditLog>where((root, criteriaQuery, cb) -> cb.conjunction())
                .and(query.getActorId() == null ? null : (root, criteriaQuery, cb) -> cb.equal(root.get("actorId"), query.getActorId()))
                .and(query.getActorUsername() == null ? null : (root, criteriaQuery, cb) -> cb.equal(root.get("actorUsername"), query.getActorUsername()))
                .and(query.getEventType() == null ? null : (root, criteriaQuery, cb) -> cb.equal(root.get("eventType"), query.getEventType()))
                .and(query.getAction() == null ? null : (root, criteriaQuery, cb) -> cb.equal(root.get("action"), query.getAction()))
                .and(query.getEntityType() == null ? null : (root, criteriaQuery, cb) -> cb.equal(root.get("entityType"), query.getEntityType()))
                .and(query.getEntityId() == null ? null : (root, criteriaQuery, cb) -> cb.equal(root.get("entityId"), query.getEntityId()))
                .and(query.getStatus() == null ? null : (root, criteriaQuery, cb) -> cb.equal(root.get("status"), query.getStatus()))
                .and(query.getRequestId() == null ? null : (root, criteriaQuery, cb) -> cb.equal(root.get("requestId"), query.getRequestId()));
    }
}
