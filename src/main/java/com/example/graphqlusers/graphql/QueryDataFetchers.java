package com.example.graphqlusers.graphql;

import com.example.graphqlusers.app.AppException;
import com.example.graphqlusers.app.ErrorCodes;
import com.example.graphqlusers.app.Role;
import com.example.graphqlusers.app.User;
import com.example.graphqlusers.app.UserPage;
import com.example.graphqlusers.auth.AuthContext;
import com.example.graphqlusers.service.UserService;
import graphql.schema.DataFetcher;

import java.util.UUID;

public class QueryDataFetchers {
    private final UserService userService;

    public QueryDataFetchers(UserService userService) {
        this.userService = userService;
    }

    public DataFetcher<User> me() {
        return environment -> {
            AuthContext auth = getAuth(environment.getContext());
            if (auth == null) {
                throw new AppException(ErrorCodes.UNAUTHORIZED, "Authentication required");
            }
            return userService.getUser(auth.userId());
        };
    }

    public DataFetcher<User> user() {
        return environment -> {
            AuthContext auth = getAuth(environment.getContext());
            if (auth == null) {
                throw new AppException(ErrorCodes.UNAUTHORIZED, "Authentication required");
            }
            UUID id = UUID.fromString(environment.getArgument("id"));
            if (!auth.hasRole(Role.ADMIN) && !auth.userId().equals(id)) {
                throw new AppException(ErrorCodes.FORBIDDEN, "Access denied");
            }
            return userService.getUser(id);
        };
    }

    public DataFetcher<UserPage> users() {
        return environment -> {
            AuthContext auth = getAuth(environment.getContext());
            if (auth == null) {
                throw new AppException(ErrorCodes.UNAUTHORIZED, "Authentication required");
            }
            if (!auth.hasRole(Role.ADMIN)) {
                throw new AppException(ErrorCodes.FORBIDDEN, "Admin access required");
            }
            int page = environment.getArgumentOrDefault("page", 0);
            int size = environment.getArgumentOrDefault("size", 20);
            return userService.listUsers(page, size);
        };
    }

    private AuthContext getAuth(Object context) {
        if (context instanceof GraphQLContext graphQLContext) {
            return graphQLContext.authContext();
        }
        return null;
    }
}
