package com.example.graphqlusers.graphql;

import com.example.graphqlusers.app.AppException;
import com.example.graphqlusers.app.AuthPayload;
import com.example.graphqlusers.app.CreateUserInput;
import com.example.graphqlusers.app.ErrorCodes;
import com.example.graphqlusers.app.RegisterInput;
import com.example.graphqlusers.app.Role;
import com.example.graphqlusers.app.UpdateUserInput;
import com.example.graphqlusers.app.User;
import com.example.graphqlusers.auth.AuthContext;
import com.example.graphqlusers.service.AuthService;
import com.example.graphqlusers.service.UserService;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MutationDataFetchers {
    private final AuthService authService;
    private final UserService userService;

    public MutationDataFetchers(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    public DataFetcher<AuthPayload> register() {
        return environment -> {
            Map<String, Object> input = environment.getArgument("input");
            RegisterInput registerInput = new RegisterInput(
                    (String) input.get("username"),
                    (String) input.get("email"),
                    (String) input.get("fullName"),
                    (String) input.get("password")
            );
            return authService.register(registerInput);
        };
    }

    public DataFetcher<AuthPayload> login() {
        return environment -> authService.login(
                environment.getArgument("usernameOrEmail"),
                environment.getArgument("password")
        );
    }

    public DataFetcher<AuthPayload> refreshToken() {
        return environment -> authService.refresh(environment.getArgument("refreshToken"));
    }

    public DataFetcher<Boolean> logout() {
        return environment -> authService.logout(environment.getArgument("refreshToken"));
    }

    public DataFetcher<User> createUser() {
        return environment -> {
            AuthContext auth = getAuth(environment.getContext());
            if (auth == null) {
                throw new AppException(ErrorCodes.UNAUTHORIZED, "Authentication required");
            }
            if (!auth.hasRole(Role.ADMIN)) {
                throw new AppException(ErrorCodes.FORBIDDEN, "Admin access required");
            }
            Map<String, Object> input = environment.getArgument("input");
            Set<Role> roles = readRoles(input.get("roles"));
            CreateUserInput createInput = new CreateUserInput(
                    (String) input.get("username"),
                    (String) input.get("email"),
                    (String) input.get("fullName"),
                    (String) input.get("password"),
                    roles
            );
            return userService.createUser(createInput);
        };
    }

    public DataFetcher<User> updateUser() {
        return environment -> {
            AuthContext auth = getAuth(environment.getContext());
            if (auth == null) {
                throw new AppException(ErrorCodes.UNAUTHORIZED, "Authentication required");
            }
            UUID id = UUID.fromString(environment.getArgument("id"));
            if (!auth.hasRole(Role.ADMIN) && !auth.userId().equals(id)) {
                throw new AppException(ErrorCodes.FORBIDDEN, "Access denied");
            }
            Map<String, Object> input = environment.getArgument("input");
            Set<Role> roles = input.containsKey("roles") ? readRoles(input.get("roles")) : null;
            UpdateUserInput updateInput = new UpdateUserInput(
                    (String) input.get("email"),
                    (String) input.get("fullName"),
                    (String) input.get("password"),
                    roles
            );
            return userService.updateUser(id, updateInput);
        };
    }

    public DataFetcher<Boolean> deleteUser() {
        return environment -> {
            AuthContext auth = getAuth(environment.getContext());
            if (auth == null) {
                throw new AppException(ErrorCodes.UNAUTHORIZED, "Authentication required");
            }
            if (!auth.hasRole(Role.ADMIN)) {
                throw new AppException(ErrorCodes.FORBIDDEN, "Admin access required");
            }
            UUID id = UUID.fromString(environment.getArgument("id"));
            if (!userService.deleteUser(id)) {
                throw new AppException(ErrorCodes.NOT_FOUND, "User not found");
            }
            return true;
        };
    }

    private AuthContext getAuth(Object context) {
        if (context instanceof GraphQLContext graphQLContext) {
            return graphQLContext.authContext();
        }
        return null;
    }

    private Set<Role> readRoles(Object rolesInput) {
        if (rolesInput == null) {
            return Set.of();
        }
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) rolesInput;
        return roles.stream().map(Role::valueOf).collect(Collectors.toSet());
    }
}
