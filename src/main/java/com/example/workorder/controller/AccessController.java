package com.example.workorder.controller;

import com.example.workorder.dto.AccessRequest;
import com.example.workorder.dto.AccessResponse;
import com.example.workorder.security.AccessControlService;
import com.example.workorder.service.AccessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accesses")
public class AccessController {

    private final AccessService accessService;
    private final RequestContextResolver requestContextResolver;
    private final AccessControlService accessControlService;

    public AccessController(AccessService accessService,
                            RequestContextResolver requestContextResolver,
                            AccessControlService accessControlService) {
        this.accessService = accessService;
        this.requestContextResolver = requestContextResolver;
        this.accessControlService = accessControlService;
    }

    @PostMapping
    public AccessResponse createAccess(@RequestHeader HttpHeaders headers, @Valid @RequestBody AccessRequest request) {
        RequestContext context = requestContextResolver.resolve(headers);
        accessControlService.verifyAdmin(context.getRole());
        return accessService.createAccess(request);
    }

    @GetMapping
    public List<AccessResponse> listAccesses() {
        return accessService.getAllAccesses();
    }

    @GetMapping("/{id}")
    public AccessResponse getAccess(@PathVariable Long id) {
        return accessService.getAccessResponse(id);
    }
}
