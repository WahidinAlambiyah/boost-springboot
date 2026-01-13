package com.alambiyah.userauth.framework.startup;

import com.alambiyah.userauth.application.service.UserService;
import com.alambiyah.userauth.domain.model.Role;
import com.alambiyah.userauth.dto.UserCreateRequest;
import com.alambiyah.userauth.framework.persistence.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;

@ApplicationScoped
public class AdminSeeder {
    @ConfigProperty(name = "app.seed.admin.enabled", defaultValue = "false")
    boolean enabled;

    @ConfigProperty(name = "app.seed.admin.username", defaultValue = "admin")
    String username;

    @ConfigProperty(name = "app.seed.admin.email", defaultValue = "admin@example.com")
    String email;

    @ConfigProperty(name = "app.seed.admin.full-name", defaultValue = "System Administrator")
    String fullName;

    @ConfigProperty(name = "app.seed.admin.password", defaultValue = "Admin123!")
    String password;

    private final UserService userService;
    private final UserRepository userRepository;

    @Inject
    public AdminSeeder(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostConstruct
    void seed() {
        if (!enabled) {
            return;
        }
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            return;
        }
        UserCreateRequest request = new UserCreateRequest(
                username,
                email,
                fullName,
                password,
                Set.of(Role.ADMIN.name()),
                true
        );
        userService.create(request);
    }
}
