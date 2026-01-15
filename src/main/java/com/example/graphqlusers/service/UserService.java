package com.example.graphqlusers.service;

import com.example.graphqlusers.app.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final List<User> users = List.of(
            new User("1", "alice", "alice@example.com", "Alice Doe"),
            new User("2", "bob", "bob@example.com", "Bob Smith"),
            new User("3", "carla", "carla@example.com", "Carla Diaz")
    );

    public List<User> listUsers() {
        return users;
    }

    public User findUser(String id) {
        return users.stream()
                .filter(user -> user.id().equals(id))
                .findFirst()
                .orElse(null);
    }
}
