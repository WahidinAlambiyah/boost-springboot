package com.example.workorder.controller;

import com.example.workorder.dto.RoleRequest;
import com.example.workorder.dto.RoleResponse;
import com.example.workorder.security.AccessControlService;
import com.example.workorder.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final RequestContextResolver requestContextResolver;
    private final AccessControlService accessControlService;

    public RoleController(RoleService roleService,
                          RequestContextResolver requestContextResolver,
                          AccessControlService accessControlService) {
        this.roleService = roleService;
        this.requestContextResolver = requestContextResolver;
        this.accessControlService = accessControlService;
    }

    @PostMapping
    public RoleResponse createRole(@RequestHeader HttpHeaders headers, @Valid @RequestBody RoleRequest request) {
        RequestContext context = requestContextResolver.resolve(headers);
        accessControlService.verifyAdmin(context.getRole());
        return roleService.createRole(request);
    }

    @GetMapping
    public List<RoleResponse> listRoles() {
        return roleService.getAllRoles();
    }

    @GetMapping("/{id}")
    public RoleResponse getRole(@PathVariable Long id) {
        return roleService.getRoleResponse(id);
    }
}
