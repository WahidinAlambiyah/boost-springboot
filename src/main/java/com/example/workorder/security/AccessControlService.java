package com.example.workorder.security;

import com.example.workorder.exception.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccessControlService {

    public void verifyDivisionAccess(UserRole role, Optional<Long> requesterDivisionId, Long targetDivisionId) {
        if (role == UserRole.ADMIN) {
            return;
        }
        if (role == UserRole.MANAGER || role == UserRole.STAFF) {
            if (requesterDivisionId.isPresent() && requesterDivisionId.get().equals(targetDivisionId)) {
                return;
            }
        }
        throw new AccessDeniedException("User is not allowed to access this division");
    }

    public void verifyAdmin(UserRole role) {
        if (role != UserRole.ADMIN) {
            throw new AccessDeniedException("Only administrators can perform this operation");
        }
    }
}
