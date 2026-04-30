package com.example.boost.scheduling.api;

import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.ClassSessionCoachResponse;
import com.example.boost.domain.dto.ClassSessionCoachUpsertRequest;
import com.example.boost.domain.dto.ClassSessionConflictCheckResponse;
import com.example.boost.domain.dto.ClassSessionCrudRequest;
import com.example.boost.domain.dto.ClassSessionResponse;
import com.example.boost.scheduling.application.ClassSessionCoachService;
import com.example.boost.scheduling.application.ClassSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "8. Class Session")
@RequestMapping("/api/class-sessions")
@RequiredArgsConstructor
public class ClassSessionController {
    private final ClassSessionService classSessionService;
    private final ClassSessionCoachService classSessionCoachService;

    @GetMapping
    @Operation(summary = "List class sessions")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Class session berhasil diambil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<ClassSessionResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Class sessions retrieved", classSessionService.list()));
    }

    @PostMapping
    @Operation(summary = "Create class session")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Class session created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validation failed")
    })
    public ResponseEntity<ApiResponse<ClassSessionResponse>> create(@Valid @RequestBody ClassSessionCrudRequest request) {
        ClassSessionResponse response = classSessionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Class session created", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get class session by id")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Class session retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<ClassSessionResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Class session retrieved", classSessionService.get(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update class session")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Class session updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validation failed")
    })
    public ResponseEntity<ApiResponse<ClassSessionResponse>> update(@PathVariable UUID id, @Valid @RequestBody ClassSessionCrudRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Class session updated", classSessionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete class session")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable UUID id) {
        classSessionService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Class session deleted", "deleted"));
    }

    @GetMapping("/{classSessionId}/coaches")
    @Operation(summary = "List session coach assignments")
    public ResponseEntity<ApiResponse<List<ClassSessionCoachResponse>>> listCoaches(@PathVariable UUID classSessionId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Session coaches retrieved", classSessionCoachService.list(classSessionId)));
    }

    @PostMapping("/{classSessionId}/coaches")
    @Operation(summary = "Create session coach assignment")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Session coach created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validation failed")
    })
    public ResponseEntity<ApiResponse<ClassSessionCoachResponse>> createCoach(@PathVariable UUID classSessionId,
                                                                               @Valid @RequestBody ClassSessionCoachUpsertRequest request) {
        ClassSessionCoachResponse response = classSessionCoachService.create(classSessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Session coach created", response));
    }

    @PutMapping("/{classSessionId}/coaches/{sessionCoachId}")
    @Operation(summary = "Update session coach assignment")
    public ResponseEntity<ApiResponse<ClassSessionCoachResponse>> updateCoach(@PathVariable UUID classSessionId,
                                                                               @PathVariable UUID sessionCoachId,
                                                                               @Valid @RequestBody ClassSessionCoachUpsertRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Session coach updated",
                classSessionCoachService.update(classSessionId, sessionCoachId, request)));
    }

    @DeleteMapping("/{classSessionId}/coaches/{sessionCoachId}")
    @Operation(summary = "Delete session coach assignment")
    public ResponseEntity<ApiResponse<String>> deleteCoach(@PathVariable UUID classSessionId, @PathVariable UUID sessionCoachId) {
        classSessionCoachService.softDelete(classSessionId, sessionCoachId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Session coach deleted", "deleted"));
    }

    @GetMapping("/conflicts")
    @Operation(summary = "Check class session conflicts")
    public ResponseEntity<ApiResponse<ClassSessionConflictCheckResponse>> conflicts(
            @RequestParam UUID classGroupId,
            @RequestParam LocalDate sessionDate,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) List<UUID> coachIds,
            @RequestParam(required = false) UUID excludeSessionId
    ) {
        ClassSessionCrudRequest request = new ClassSessionCrudRequest(classGroupId, sessionDate, startTime, endTime, locationId, coachIds, null);
        ClassSessionConflictCheckResponse response = classSessionService.checkConflicts(request, excludeSessionId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response.message(), response));
    }
}
