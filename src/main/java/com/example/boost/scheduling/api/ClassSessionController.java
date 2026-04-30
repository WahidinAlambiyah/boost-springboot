package com.example.boost.scheduling.api;

import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.ClassSessionConflictCheckResponse;
import com.example.boost.domain.dto.ClassSessionCrudRequest;
import com.example.boost.domain.dto.ClassSessionResponse;
import com.example.boost.scheduling.application.ClassSessionService;
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
@RequestMapping("/api/class-sessions")
@RequiredArgsConstructor
public class ClassSessionController {
    private final ClassSessionService classSessionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassSessionResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Class sessions retrieved", classSessionService.list()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClassSessionResponse>> create(@Valid @RequestBody ClassSessionCrudRequest request) {
        ClassSessionResponse response = classSessionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Class session created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassSessionResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Class session retrieved", classSessionService.get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassSessionResponse>> update(@PathVariable UUID id, @Valid @RequestBody ClassSessionCrudRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Class session updated", classSessionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable UUID id) {
        classSessionService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Class session deleted", "deleted"));
    }

    @GetMapping("/conflicts")
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
