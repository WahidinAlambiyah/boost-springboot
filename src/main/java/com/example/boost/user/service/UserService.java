package com.example.boost.user.service;

import com.example.boost.auth.JwtService;
import com.example.boost.event.service.DomainEventService;
import com.example.boost.user.dto.LoginRequest;
import com.example.boost.user.dto.RegisterRequest;
import com.example.boost.user.model.User;
import com.example.boost.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class UserService {

    private final UserRepository repository;
    private final Supplier<String> ulidSupplier;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtService jwtService;
    private final DomainEventService eventService;

    public UserService(UserRepository repository, Supplier<String> ulidSupplier, JwtService jwtService, DomainEventService eventService) {
        this.repository = repository;
        this.ulidSupplier = ulidSupplier;
        this.jwtService = jwtService;
        this.eventService = eventService;
    }

    @Transactional
    public User register(RegisterRequest request) {
        User user = new User();
        user.setId(ulidSupplier.get());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setPassword(encoder.encode(request.getPassword()));
        User saved = repository.save(user);
        eventService.record("User", saved.getId(), "USER_REGISTERED", Map.of("username", saved.getUsername(), "role", saved.getRole()));
        return saved;
    }

    public Optional<User> authenticate(LoginRequest request) {
        return repository.findByUsername(request.getUsername())
                .filter(user -> encoder.matches(request.getPassword(), user.getPassword()));
    }

    public String issueToken(User user) {
        return jwtService.generateToken(user.getId(), Map.of("username", user.getUsername(), "role", user.getRole().name()));
    }
}
