package com.example.graphqlusers.repository;

import com.example.graphqlusers.app.Role;
import com.example.graphqlusers.app.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);

    Optional<User> findByUsernameOrEmail(String usernameOrEmail);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> list(int page, int size);

    long count();

    User create(User user);

    User update(UUID id, String email, String fullName, String passwordHash, Set<Role> roles);

    boolean delete(UUID id);
}
