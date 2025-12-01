package com.example.workorder.controller;

import com.example.workorder.dto.UserRequest;
import com.example.workorder.dto.UserResponse;
import com.example.workorder.security.AccessControlService;
import com.example.workorder.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final RequestContextResolver requestContextResolver;
    private final AccessControlService accessControlService;

    public UserController(UserService userService,
                          RequestContextResolver requestContextResolver,
                          AccessControlService accessControlService) {
        this.userService = userService;
        this.requestContextResolver = requestContextResolver;
        this.accessControlService = accessControlService;
    }

    @PostMapping
    public UserResponse createUser(@RequestHeader HttpHeaders headers, @Valid @RequestBody UserRequest request) {
        RequestContext context = requestContextResolver.resolve(headers);
        accessControlService.verifyAdmin(context.getRole());
        return userService.createUser(request);
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUserResponse(id);
    }
}
