package com.example.boost.domain.dto;

import com.example.boost.scheduling.application.ClassSessionConflictService;

import java.util.List;

public record ClassSessionConflictCheckResponse(
        boolean hasConflict,
        List<ClassSessionConflictService.SessionConflict> conflicts,
        String message
) {
}
