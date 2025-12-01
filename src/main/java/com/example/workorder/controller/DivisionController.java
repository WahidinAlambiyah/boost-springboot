package com.example.workorder.controller;

import com.example.workorder.dto.DivisionRequest;
import com.example.workorder.dto.DivisionResponse;
import com.example.workorder.security.AccessControlService;
import com.example.workorder.security.UserRole;
import com.example.workorder.service.DivisionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/divisions")
public class DivisionController {

    private final DivisionService divisionService;
    private final AccessControlService accessControlService;
    private final RequestContextResolver contextResolver;

    public DivisionController(DivisionService divisionService, AccessControlService accessControlService,
                              RequestContextResolver contextResolver) {
        this.divisionService = divisionService;
        this.accessControlService = accessControlService;
        this.contextResolver = contextResolver;
    }

    @PostMapping
    public ResponseEntity<DivisionResponse> create(@RequestBody @Valid DivisionRequest request,
                                                   @RequestHeader HttpHeaders headers) {
        RequestContext context = contextResolver.resolve(headers);
        accessControlService.verifyAdmin(context.getRole());
        return ResponseEntity.ok(divisionService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<DivisionResponse>> findAll() {
        return ResponseEntity.ok(divisionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DivisionResponse> findOne(@PathVariable Long id) {
        var division = divisionService.getById(id);
        return ResponseEntity.ok(new DivisionResponse(division.getId(), division.getName()));
    }
}
