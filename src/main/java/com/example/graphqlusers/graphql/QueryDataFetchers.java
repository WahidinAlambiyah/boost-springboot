package com.example.graphqlusers.graphql;

import com.example.graphqlusers.app.User;
import com.example.graphqlusers.service.UserService;
import graphql.schema.DataFetcher;

import java.util.List;

public class QueryDataFetchers {
    private final UserService userService;

    public QueryDataFetchers(UserService userService) {
        this.userService = userService;
    }

    public DataFetcher<List<User>> users() {
        return environment -> userService.listUsers();
    }

    public DataFetcher<User> user() {
        return environment -> userService.findUser(environment.getArgument("id"));
    }
}
