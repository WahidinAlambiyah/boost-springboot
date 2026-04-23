package com.example.boost.catalog.repository;

import com.example.boost.catalog.dto.CatalogSummaryResponse;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CatalogRepository {
    public List<CatalogSummaryResponse> findSummaries() {
        return List.of(
                CatalogSummaryResponse.builder()
                        .code("CATALOG-001")
                        .name("Default Catalog Record")
                        .status("ACTIVE")
                        .build()
        );
    }
}
