package com.example.boost.catalog.application.port;

import com.example.boost.domain.entity.ClassGroup;

import java.util.UUID;

public interface CatalogQueryService {
    ClassGroup getById(UUID id);

    void touchUpdatedAt(ClassGroup classGroup);
}
