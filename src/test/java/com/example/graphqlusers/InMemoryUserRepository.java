package com.example.graphqlusers;

import com.example.graphqlusers.app.Role;
import com.example.graphqlusers.app.User;
import com.example.graphqlusers.repository.UserRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {
    private final Map<UUID, User> users = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return users.values().stream()
                .filter(user -> user.username().equals(usernameOrEmail) || user.email().equals(usernameOrEmail))
                .findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(user -> user.username().equals(username))
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.email().equals(email))
                .findFirst();
    }

    @Override
    public List<User> list(int page, int size) {
        return new ArrayList<>(users.values());
    }

    @Override
    public long count() {
        return users.size();
    }

    @Override
    public User create(User user) {
        users.put(user.id(), user);
        return user;
    }

    @Override
    public User update(UUID id, String email, String fullName, String passwordHash, Set<Role> roles) {
        User existing = users.get(id);
        if (existing == null) {
            return null;
        }
        User updated = new User(
                existing.id(),
                existing.username(),
                email == null ? existing.email() : email,
                fullName == null ? existing.fullName() : fullName,
                passwordHash == null ? existing.passwordHash() : passwordHash,
                roles == null ? existing.roles() : roles,
                existing.createdAt(),
                Instant.now()
        );
        users.put(id, updated);
        return updated;
    }

    @Override
    public boolean delete(UUID id) {
        return users.remove(id) != null;
    }
}
