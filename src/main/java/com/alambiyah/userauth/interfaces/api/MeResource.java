package com.alambiyah.userauth.interfaces.api;

import com.alambiyah.userauth.application.service.UserService;
import com.alambiyah.userauth.dto.UserResponse;
import com.alambiyah.userauth.framework.cache.UserCache;
import com.alambiyah.userauth.framework.mapper.UserMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

@Path("/api/me")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"USER", "ADMIN"})
public class MeResource {
    private final JsonWebToken jsonWebToken;
    private final UserService userService;
    private final UserCache userCache;
    private final UserMapper userMapper;

    @Inject
    public MeResource(JsonWebToken jsonWebToken, UserService userService, UserCache userCache, UserMapper userMapper) {
        this.jsonWebToken = jsonWebToken;
        this.userService = userService;
        this.userCache = userCache;
        this.userMapper = userMapper;
    }

    @GET
    public UserResponse me() {
        UUID userId = UUID.fromString(jsonWebToken.getSubject());
        return userCache.get(userId)
                .orElseGet(() -> {
                    var user = userService.getById(userId);
                    userCache.put(user);
                    return userMapper.toResponse(user);
                });
    }
}
