package com.example.boost.catalog.service;

import com.example.boost.catalog.dto.CatalogSummaryResponse;
import com.example.boost.catalog.repository.CatalogRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final CatalogRepository catalogRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('CLASS_READ')")
    public List<CatalogSummaryResponse> getSummaries() {
        return catalogRepository.findSummaries();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('CLASS_WRITE')")
    public long getWritableSummaryCount() {
        return catalogRepository.findSummaries().size();
    }
}
