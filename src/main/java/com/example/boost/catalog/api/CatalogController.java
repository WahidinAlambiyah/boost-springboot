package com.example.boost.catalog.api;

import com.example.boost.domain.dto.CatalogSummaryResponse;
import com.example.boost.catalog.application.CatalogService;
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
@Tag(name = "7. Catalog")
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalogService;

    @GetMapping
    @PreAuthorize("hasAuthority('CLASS_READ')")
    @Operation(
            summary = "List catalog summaries",
            description = "Mengambil ringkasan katalog kelas. Authority utama: CLASS_READ."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Catalog berhasil diambil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority CLASS_READ")
    })
    public ResponseEntity<ApiResponse<List<CatalogSummaryResponse>>> getSummaries() {
        List<CatalogSummaryResponse> responses = catalogService.getSummaries();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Catalog items retrieved", responses));
    }

    @GetMapping("/summary-count")
    @PreAuthorize("hasAuthority('CLASS_WRITE')")
    @Operation(
            summary = "Get writable catalog summary count",
            description = "Mengambil jumlah item katalog yang bisa dikelola. Authority utama: CLASS_WRITE."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Jumlah catalog berhasil diambil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority CLASS_WRITE")
    })
    public ResponseEntity<ApiResponse<Long>> getWritableSummaryCount() {
        long count = catalogService.getWritableSummaryCount();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Catalog writable summary count retrieved", count));
    }
}
