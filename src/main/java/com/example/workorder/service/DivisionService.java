package com.example.workorder.service;

import com.example.workorder.domain.Division;
import com.example.workorder.dto.DivisionRequest;
import com.example.workorder.dto.DivisionResponse;
import com.example.workorder.exception.NotFoundException;
import com.example.workorder.repository.DivisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DivisionService {

    private final DivisionRepository divisionRepository;

    public DivisionService(DivisionRepository divisionRepository) {
        this.divisionRepository = divisionRepository;
    }

    public DivisionResponse create(DivisionRequest request) {
        Division division = new Division();
        division.setName(request.getName());
        Division saved = divisionRepository.save(division);
        return new DivisionResponse(saved.getId(), saved.getName());
    }

    public List<DivisionResponse> findAll() {
        return divisionRepository.findAll().stream()
                .map(d -> new DivisionResponse(d.getId(), d.getName()))
                .collect(Collectors.toList());
    }

    public Division getById(Long id) {
        return divisionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Division not found: " + id));
    }
}
