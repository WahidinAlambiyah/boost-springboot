package com.example.workorder.service;

import com.example.workorder.domain.Access;
import com.example.workorder.dto.AccessRequest;
import com.example.workorder.dto.AccessResponse;
import com.example.workorder.exception.NotFoundException;
import com.example.workorder.repository.AccessRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccessService {

    private final AccessRepository accessRepository;

    public AccessService(AccessRepository accessRepository) {
        this.accessRepository = accessRepository;
    }

    public AccessResponse createAccess(AccessRequest request) {
        Access access = new Access();
        access.setCode(request.getCode());
        access.setDescription(request.getDescription());
        Access saved = accessRepository.save(access);
        return toResponse(saved);
    }

    public List<AccessResponse> getAllAccesses() {
        return accessRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Access getAccess(Long id) {
        return accessRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Access not found with id " + id));
    }

    public AccessResponse getAccessResponse(Long id) {
        return toResponse(getAccess(id));
    }

    private AccessResponse toResponse(Access access) {
        return new AccessResponse(access.getId(), access.getCode(), access.getDescription());
    }
}
