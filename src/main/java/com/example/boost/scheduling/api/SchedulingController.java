package com.example.boost.scheduling.api;

import com.example.boost.domain.dto.SchedulingSummaryResponse;
import com.example.boost.domain.dto.ClassRescheduleRequest;
import com.example.boost.scheduling.application.SchedulingService;
import com.example.boost.common.api.ApiResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scheduling")
@RequiredArgsConstructor
public class SchedulingController {
    private final SchedulingService schedulingService;

    @GetMapping
    @PreAuthorize("hasAuthority('SCHEDULE_READ')")
    public ResponseEntity<ApiResponse<List<SchedulingSummaryResponse>>> getSummaries() {
        List<SchedulingSummaryResponse> responses = schedulingService.getSummaries();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Schedules retrieved", responses));
    }

    @GetMapping("/summary-count")
    @PreAuthorize("hasAuthority('SCHEDULE_WRITE')")
    public ResponseEntity<ApiResponse<Long>> getWritableSummaryCount() {
        long count = schedulingService.getWritableSummaryCount();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Scheduling writable summary count retrieved", count));
    }

    @PostMapping("/reschedule")
    @PreAuthorize("hasAuthority('SCHEDULE_WRITE')")
    public ResponseEntity<ApiResponse<String>> reschedule(@Valid @RequestBody ClassRescheduleRequest request) {
        schedulingService.reschedule(
                request.classGroupId(),
                request.previousStartAt().toString(),
                request.newStartAt().toString(),
                request.reason()
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(HttpStatus.ACCEPTED.value(), "Class rescheduled event captured", "queued"));
    }
}
