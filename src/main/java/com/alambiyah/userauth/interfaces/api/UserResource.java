package com.alambiyah.userauth.interfaces.api;

import com.alambiyah.userauth.application.service.UserService;
import com.alambiyah.userauth.dto.UserCreateRequest;
import com.alambiyah.userauth.dto.UserResponse;
import com.alambiyah.userauth.dto.UserUpdateRequest;
import com.alambiyah.userauth.framework.mapper.UserMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/api/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class UserResource {
    private final UserService userService;
    private final UserMapper userMapper;

    @Inject
    public UserResource(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GET
    public List<UserResponse> list() {
        return userService.listAll().stream().map(userMapper::toResponse).toList();
    }

    @GET
    @Path("/{id}")
    public UserResponse getById(@PathParam("id") UUID id) {
        return userMapper.toResponse(userService.getById(id));
    }

    @POST
    public Response create(@Valid UserCreateRequest request) {
        UserResponse response = userMapper.toResponse(userService.create(request));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    public UserResponse update(@PathParam("id") UUID id, @Valid UserUpdateRequest request) {
        return userMapper.toResponse(userService.update(id, request));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        userService.delete(id);
        return Response.noContent().build();
    }
}
