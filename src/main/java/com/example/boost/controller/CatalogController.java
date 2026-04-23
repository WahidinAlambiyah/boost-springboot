package com.example.boost.controller;

import com.example.boost.domain.dto.CatalogSummaryResponse;
import com.example.boost.service.CatalogService;
import com.example.boost.domain.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalogService;

    @GetMapping
    @PreAuthorize("hasAuthority('CLASS_READ')")
    public ResponseEntity<ApiResponse<List<CatalogSummaryResponse>>> getSummaries() {
        List<CatalogSummaryResponse> responses = catalogService.getSummaries();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Catalog items retrieved", responses));
    }

    @GetMapping("/summary-count")
    @PreAuthorize("hasAuthority('CLASS_WRITE')")
    public ResponseEntity<ApiResponse<Long>> getWritableSummaryCount() {
        long count = catalogService.getWritableSummaryCount();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Catalog writable summary count retrieved", count));
    }
}
