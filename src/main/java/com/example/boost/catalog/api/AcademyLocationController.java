package com.example.boost.catalog.api;

import com.example.boost.catalog.application.AcademyLocationService;
import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.AcademyLocationCreateRequest;
import com.example.boost.domain.dto.AcademyLocationResponse;
import com.example.boost.domain.dto.AcademyLocationUpdateRequest;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/academy-locations")
@RequiredArgsConstructor
@Tag(name = "Academy Location Management")
public class AcademyLocationController {
    private final AcademyLocationService academyLocationService;

    @GetMapping
    @PreAuthorize("hasAuthority('LOCATION_READ')")
    @Operation(summary = "List academy locations")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Academy locations retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<Page<AcademyLocationResponse>>> list(@RequestParam UUID academyId,
                                                                           @RequestParam(required = false) String keyword,
                                                                           Pageable pageable) {
        Page<AcademyLocationResponse> responses = academyLocationService.list(academyId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Academy locations retrieved", responses));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('LOCATION_WRITE')")
    @Operation(summary = "Create academy location")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Academy location created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<AcademyLocationResponse>> create(
            @Valid @RequestBody AcademyLocationCreateRequest request) {
        AcademyLocationResponse response = academyLocationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Academy location created", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOCATION_READ')")
    @Operation(summary = "Get academy location by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Academy location retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<AcademyLocationResponse>> getById(@PathVariable UUID id) {
        AcademyLocationResponse response = academyLocationService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Academy location retrieved", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LOCATION_WRITE')")
    @Operation(summary = "Update academy location")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Academy location updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<AcademyLocationResponse>> update(@PathVariable UUID id,
                                                                       @Valid @RequestBody AcademyLocationUpdateRequest request) {
        AcademyLocationResponse response = academyLocationService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Academy location updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LOCATION_WRITE')")
    @Operation(summary = "Delete academy location")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Academy location deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        academyLocationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
