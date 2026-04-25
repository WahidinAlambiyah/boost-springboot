package com.example.boost.scheduling.api;

import com.example.boost.domain.dto.SchedulingSummaryResponse;
import com.example.boost.domain.dto.ClassRescheduleRequest;
import com.example.boost.scheduling.application.SchedulingService;
import com.example.boost.common.api.ApiResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "8. Scheduling")
@RequestMapping("/api/scheduling")
@RequiredArgsConstructor
public class SchedulingController {
    private final SchedulingService schedulingService;

    @GetMapping
    @PreAuthorize("hasAuthority('SCHEDULE_READ')")
    @Operation(
            summary = "List scheduling summaries",
            description = "Mengambil ringkasan jadwal kelas. Authority utama: SCHEDULE_READ."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Jadwal berhasil diambil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority SCHEDULE_READ")
    })
    public ResponseEntity<ApiResponse<List<SchedulingSummaryResponse>>> getSummaries() {
        List<SchedulingSummaryResponse> responses = schedulingService.getSummaries();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Schedules retrieved", responses));
    }

    @GetMapping("/summary-count")
    @PreAuthorize("hasAuthority('SCHEDULE_WRITE')")
    @Operation(
            summary = "Get writable scheduling summary count",
            description = "Mengambil jumlah ringkasan jadwal yang bisa diubah. Authority utama: SCHEDULE_WRITE."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Jumlah scheduling berhasil diambil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority SCHEDULE_WRITE")
    })
    public ResponseEntity<ApiResponse<Long>> getWritableSummaryCount() {
        long count = schedulingService.getWritableSummaryCount();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Scheduling writable summary count retrieved", count));
    }

    @PostMapping("/reschedule")
    @PreAuthorize("hasAuthority('SCHEDULE_WRITE')")
    @Operation(
            summary = "Reschedule class",
            description = "Mengirim event perubahan jadwal kelas secara asynchronous. Authority utama: SCHEDULE_WRITE."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Perubahan jadwal berhasil di-submit"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority SCHEDULE_WRITE"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validasi payload gagal")
    })
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
