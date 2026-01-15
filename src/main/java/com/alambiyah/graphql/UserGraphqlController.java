package com.alambiyah.graphql;

import com.alambiyah.app.User;
import com.alambiyah.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class UserGraphqlController {
    private final UserService userService;

    public UserGraphqlController(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    public List<User> users() {
        return userService.listUsers();
    }

    @QueryMapping
    public User user(@Argument String id) {
        return userService.findUser(id);
    }
}
