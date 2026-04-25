package com.example.boost.scheduling.api;

import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.EnrollmentActionResponse;
import com.example.boost.domain.dto.EnrollmentRegisterRequest;
import com.example.boost.domain.dto.EnrollmentSummaryResponse;
import com.example.boost.scheduling.application.EnrollmentService;
import com.example.boost.common.util.HeaderUtils;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "9. Enrollment")
@RequestMapping("/api/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('ENROLLMENT_READ')")
    @Operation(
            summary = "List enrollment summaries",
            description = "Mengambil ringkasan enrollment. Authority utama: ENROLLMENT_READ."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Enrollment berhasil diambil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority ENROLLMENT_READ")
    })
    public ResponseEntity<ApiResponse<List<EnrollmentSummaryResponse>>> getSummaries() {
        List<EnrollmentSummaryResponse> responses = enrollmentService.getSummaries();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Enrollments retrieved", responses));
    }

    @GetMapping("/summary-count")
    @PreAuthorize("hasAuthority('ENROLLMENT_WRITE')")
    @Operation(
            summary = "Get writable enrollment summary count",
            description = "Mengambil jumlah ringkasan enrollment yang bisa ditulis. Authority utama: ENROLLMENT_WRITE."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Jumlah enrollment berhasil diambil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority ENROLLMENT_WRITE")
    })
    public ResponseEntity<ApiResponse<Long>> getWritableSummaryCount() {
        long count = enrollmentService.getWritableSummaryCount();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Enrollment writable summary count retrieved", count));
    }

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('ENROLLMENT_WRITE')")
    @Operation(
            summary = "Register enrollment",
            description = "Mendaftarkan siswa ke kelas dengan idempotency key. Authority utama: ENROLLMENT_WRITE."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Enrollment berhasil diproses"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority ENROLLMENT_WRITE"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validasi payload gagal")
    })
    public ResponseEntity<ApiResponse<EnrollmentActionResponse>> register(
            @Valid @RequestBody EnrollmentRegisterRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        EnrollmentActionResponse response = enrollmentService.register(request.studentId(), request.classGroupId(), HeaderUtils.normalize(idempotencyKey));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Enrollment processed", response));
    }

    @PostMapping("/{enrollmentId}/cancel")
    @PreAuthorize("hasAuthority('ENROLLMENT_WRITE')")
    @Operation(
            summary = "Cancel enrollment",
            description = "Membatalkan enrollment dan mempromosikan waitlist bila ada. Authority utama: ENROLLMENT_WRITE."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Enrollment berhasil dibatalkan"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority ENROLLMENT_WRITE")
    })
    public ResponseEntity<ApiResponse<EnrollmentActionResponse>> cancel(@PathVariable UUID enrollmentId) {
        EnrollmentActionResponse response = enrollmentService.cancelAndPromote(enrollmentId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Enrollment cancelled and waitlist promoted", response));
    }
}
