package com.example.boost.notification.api;

import com.example.boost.domain.dto.NotificationSummaryResponse;
import com.example.boost.notification.application.NotificationService;
import com.example.boost.common.api.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "12. Notification")
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_READ')")
    @Operation(
            summary = "List notification summaries",
            description = "Mengambil ringkasan notifikasi. Authority utama: NOTIFICATION_READ."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifikasi berhasil diambil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority NOTIFICATION_READ")
    })
    public ResponseEntity<ApiResponse<List<NotificationSummaryResponse>>> getSummaries() {
        List<NotificationSummaryResponse> responses = notificationService.getSummaries();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Notifications retrieved", responses));
    }

    @GetMapping("/summary-count")
    @PreAuthorize("hasAuthority('NOTIFICATION_WRITE')")
    @Operation(
            summary = "Get writable notification summary count",
            description = "Mengambil jumlah notifikasi yang bisa dikelola. Authority utama: NOTIFICATION_WRITE."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Jumlah notifikasi berhasil diambil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority NOTIFICATION_WRITE")
    })
    public ResponseEntity<ApiResponse<Long>> getWritableSummaryCount() {
        long count = notificationService.getWritableSummaryCount();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Notification writable summary count retrieved", count));
    }
}
