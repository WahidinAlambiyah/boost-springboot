package com.alambiyah.userauth.framework.persistence;

import com.alambiyah.userauth.domain.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return find("username = ?1 or email = ?1", usernameOrEmail).firstResultOptional();
    }

    public Optional<User> findOptionalById(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }
}
