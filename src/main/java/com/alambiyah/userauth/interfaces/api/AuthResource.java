package com.alambiyah.userauth.interfaces.api;

import com.alambiyah.userauth.application.service.AuthService;
import com.alambiyah.userauth.dto.AuthResponse;
import com.alambiyah.userauth.dto.LoginRequest;
import com.alambiyah.userauth.dto.LogoutRequest;
import com.alambiyah.userauth.dto.RefreshRequest;
import com.alambiyah.userauth.dto.RegisterRequest;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class AuthResource {
    private final AuthService authService;

    @Inject
    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("/login")
    public AuthResponse login(@Valid LoginRequest request) {
        return authService.login(request);
    }

    @POST
    @Path("/refresh")
    public AuthResponse refresh(@Valid RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @POST
    @Path("/logout")
    public Response logout(@Valid LogoutRequest request) {
        authService.logout(request.refreshToken());
        return Response.noContent().build();
    }
}
