package com.example.boost.dashboard.api;

import com.example.boost.common.api.ApiResponse;
import com.example.boost.dashboard.application.DashboardService;
import com.example.boost.domain.dto.CoachDashboardResponse;
import com.example.boost.domain.dto.EventDashboardResponse;
import com.example.boost.domain.dto.OwnerDashboardResponse;
import com.example.boost.domain.dto.ParentDashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@Tag(name = "Dashboard", description = "Aggregated dashboard contracts for owner, coach, parent, and event reporting")
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/owner")
    // TODO: remove fallback permissions after DASHBOARD_OWNER_READ is assigned to all required roles in production.
    @PreAuthorize("hasAnyAuthority('DASHBOARD_OWNER_READ','REPORT_PROGRESS_READ','ACADEMY_READ')")
    @Operation(summary = "Get owner dashboard", description = "Returns academy summary cards, today's sessions, attention list, events, and recent assessments.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Owner dashboard retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires DASHBOARD_OWNER_READ")
    })
    public ResponseEntity<ApiResponse<OwnerDashboardResponse>> owner(
            @RequestParam UUID academyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Owner dashboard retrieved", dashboardService.owner(academyId, from, to)));
    }

    @GetMapping("/coach")
    // TODO: remove fallback permissions after DASHBOARD_COACH_READ is assigned to all required roles in production.
    @PreAuthorize("hasAnyAuthority('DASHBOARD_COACH_READ','ATTENDANCE_MARK','ASSESSMENT_WRITE')")
    @Operation(summary = "Get coach dashboard", description = "Returns the authenticated coach's assigned sessions and pending work.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Coach dashboard retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires DASHBOARD_COACH_READ")
    })
    public ResponseEntity<ApiResponse<CoachDashboardResponse>> coach(
            @RequestParam UUID academyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach dashboard retrieved", dashboardService.coach(academyId, from, to)));
    }

    @GetMapping("/parent")
    // TODO: enforce guardian/student ownership when parent self-service is enabled.
    @PreAuthorize("hasAnyAuthority('DASHBOARD_PARENT_READ','REPORT_PROGRESS_READ')")
    @Operation(summary = "Get parent dashboard", description = "Returns attendance, latest progress, skills, and upcoming sessions for a student.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Parent dashboard retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires DASHBOARD_PARENT_READ")
    })
    public ResponseEntity<ApiResponse<ParentDashboardResponse>> parent(
            @RequestParam UUID studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Parent dashboard retrieved", dashboardService.parent(studentId, from, to)));
    }

    @GetMapping("/event")
    @PreAuthorize("hasAnyAuthority('DASHBOARD_EVENT_READ','EVENT_READ','REPORT_EVENT_READ')")
    @Operation(summary = "Get event dashboard", description = "Returns event dashboard contract. Empty zero-summary until event module is available.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Event dashboard retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires DASHBOARD_EVENT_READ")
    })
    public ResponseEntity<ApiResponse<EventDashboardResponse>> event(
            @RequestParam UUID academyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Event dashboard retrieved", dashboardService.event(academyId, from, to)));
    }
}
