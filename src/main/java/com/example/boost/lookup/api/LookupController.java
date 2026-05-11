package com.example.boost.lookup.api;

import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.LookupOptionResponse;
import com.example.boost.lookup.application.LookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Lookup", description = "Lightweight dropdown/select options for frontend forms")
@RequestMapping("/api/lookups")
public class LookupController {
    private final LookupService lookupService;

    public LookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("/academies")
    @PreAuthorize("hasAnyAuthority('LOOKUP_READ','ACADEMY_READ')")
    @Operation(summary = "Lookup active academies")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Academy lookup retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires LOOKUP_READ or ACADEMY_READ")
    })
    public ResponseEntity<ApiResponse<List<LookupOptionResponse>>> academies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer limit
    ) {
        return ok("Academy lookup retrieved", lookupService.academies(search, limit));
    }

    @GetMapping("/academy-locations")
    @PreAuthorize("hasAnyAuthority('LOOKUP_READ','LOCATION_READ','ACADEMY_READ')")
    @Operation(summary = "Lookup active academy locations")
    public ResponseEntity<ApiResponse<List<LookupOptionResponse>>> academyLocations(
            @RequestParam(required = false) UUID academyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer limit
    ) {
        return ok("Academy location lookup retrieved", lookupService.academyLocations(academyId, search, limit));
    }

    @GetMapping("/coaches")
    @PreAuthorize("hasAnyAuthority('LOOKUP_READ','COACH_READ')")
    @Operation(summary = "Lookup active coaches")
    public ResponseEntity<ApiResponse<List<LookupOptionResponse>>> coaches(
            @RequestParam(required = false) UUID academyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer limit
    ) {
        return ok("Coach lookup retrieved", lookupService.coaches(academyId, search, limit));
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyAuthority('LOOKUP_READ','ENROLLMENT_READ','ATTENDANCE_READ','REPORT_PROGRESS_READ')")
    @Operation(summary = "Lookup active students")
    public ResponseEntity<ApiResponse<List<LookupOptionResponse>>> students(
            @RequestParam(required = false) UUID academyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer limit
    ) {
        return ok("Student lookup retrieved", lookupService.students(academyId, status, level, search, limit));
    }

    @GetMapping("/class-groups")
    @PreAuthorize("hasAnyAuthority('LOOKUP_READ','CLASS_READ')")
    @Operation(summary = "Lookup active class groups")
    public ResponseEntity<ApiResponse<List<LookupOptionResponse>>> classGroups(
            @RequestParam(required = false) UUID academyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer limit
    ) {
        return ok("Class group lookup retrieved", lookupService.classGroups(academyId, search, limit));
    }

    @GetMapping("/class-sessions")
    @PreAuthorize("hasAnyAuthority('LOOKUP_READ','SCHEDULE_READ','CLASS_READ')")
    @Operation(summary = "Lookup active class sessions")
    public ResponseEntity<ApiResponse<List<LookupOptionResponse>>> classSessions(
            @RequestParam(required = false) UUID academyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer limit
    ) {
        return ok("Class session lookup retrieved", lookupService.classSessions(academyId, date, status, search, limit));
    }

    @GetMapping("/assessment-skills")
    @PreAuthorize("hasAnyAuthority('LOOKUP_READ','ASSESSMENT_READ','ASSESSMENT_WRITE')")
    @Operation(summary = "Lookup active assessment skills")
    public ResponseEntity<ApiResponse<List<LookupOptionResponse>>> assessmentSkills(
            @RequestParam(required = false) UUID academyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer limit
    ) {
        return ok("Assessment skill lookup retrieved", lookupService.assessmentSkills(academyId, search, limit));
    }

    @GetMapping("/training-packages")
    @PreAuthorize("hasAnyAuthority('LOOKUP_READ','PACKAGE_READ')")
    @Operation(summary = "Lookup active training packages")
    public ResponseEntity<ApiResponse<List<LookupOptionResponse>>> trainingPackages(
            @RequestParam(required = false) UUID academyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer limit
    ) {
        return ok("Training package lookup retrieved", lookupService.trainingPackages(academyId, search, limit));
    }

    @GetMapping("/events")
    @PreAuthorize("hasAnyAuthority('LOOKUP_READ','EVENT_READ','REPORT_EVENT_READ','DASHBOARD_EVENT_READ')")
    @Operation(summary = "Lookup active events", description = "Returns an empty list until the event module is available.")
    public ResponseEntity<ApiResponse<List<LookupOptionResponse>>> events(
            @RequestParam(required = false) UUID academyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer limit
    ) {
        return ok("Event lookup retrieved", lookupService.events(academyId, search, limit));
    }

    private ResponseEntity<ApiResponse<List<LookupOptionResponse>>> ok(String message, List<LookupOptionResponse> data) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), message, data));
    }
}
