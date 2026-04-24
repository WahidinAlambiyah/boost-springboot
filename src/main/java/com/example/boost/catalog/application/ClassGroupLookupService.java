package com.example.boost.catalog.application;

import com.example.boost.catalog.application.port.CatalogQueryService;
import com.example.boost.catalog.infrastructure.ClassGroupRepository;
import com.example.boost.domain.entity.ClassGroup;
import com.example.boost.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassGroupLookupService implements CatalogQueryService {
    private final ClassGroupRepository classGroupRepository;

    @Override
    @Transactional(readOnly = true)
    public ClassGroup getById(UUID id) {
        return classGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Class group not found"));
    }

    @Override
    @Transactional
    public void touchUpdatedAt(ClassGroup classGroup) {
        classGroup.setUpdatedAt(OffsetDateTime.now());
        classGroupRepository.save(classGroup);
    }
}
