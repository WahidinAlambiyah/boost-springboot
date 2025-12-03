package com.example.boost.config;

import com.example.boost.domain.Role;
import com.example.boost.domain.User;
import com.example.boost.repository.RoleRepository;
import com.example.boost.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class AdminDataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:password}")
    private String adminPassword;

    public AdminDataInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role adminRole = ensureRoleExists("ADMIN", "Administrator");
        ensureRoleExists("USER", "Standard user");

        Optional<User> existingAdmin = userRepository.findByUsername(adminUsername);
        User admin = existingAdmin.orElseGet(User::new);
        admin.setUsername(adminUsername);
        admin.setEmail(existingAdmin.map(User::getEmail).orElse("admin@example.com"));
        admin.setFullName(existingAdmin.map(User::getFullName).orElse("Admin Account"));
        admin.setPhoneNumber(existingAdmin.map(User::getPhoneNumber).orElse("0000000000"));
        admin.setAgreementAt(existingAdmin.map(User::getAgreementAt).orElse(Instant.now()));
        admin.setActive(true);

        if (existingAdmin.isEmpty() || admin.getPasswordHash() == null ||
                !passwordEncoder.matches(adminPassword, admin.getPasswordHash())) {
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        }

        Set<Role> roles = new HashSet<>(admin.getRoles());
        roles.add(adminRole);
        admin.setRoles(roles);

        userRepository.save(admin);
    }

    private Role ensureRoleExists(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name, description)));
    }
}
