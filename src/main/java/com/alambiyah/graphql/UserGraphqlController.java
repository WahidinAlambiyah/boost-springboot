package com.alambiyah.graphql;

import com.alambiyah.app.Role;
import com.alambiyah.app.User;
import com.alambiyah.repository.RoleRepository;
import com.alambiyah.repository.UserRepository;
import com.alambiyah.service.UserService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
@Transactional
public class UserGraphqlController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserGraphqlController(
            UserService userService,
            UserRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @QueryMapping
    public List<User> users() {
        return userService.listUsers();
    }

    @QueryMapping
    public User user(@Argument String id) {
        return userService.findUser(id);
    }

    @QueryMapping
    public List<Role> roles() {
        return roleRepository.findAll();
    }

    @QueryMapping
    public Role role(@Argument String id) {
        return roleRepository.findById(UUID.fromString(id)).orElse(null);
    }

    @MutationMapping
    public User createUser(@Argument CreateUserInput input) {
        User user = new User(null, input.username(), input.email(), input.fullName());
        if (input.roleIds() != null) {
            user.setRoles(resolveRoles(input.roleIds()));
        }
        return userRepository.save(user);
    }

    @MutationMapping
    public User updateUser(@Argument String id, @Argument UpdateUserInput input) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (input.username() != null) {
            user.setUsername(input.username());
        }
        if (input.email() != null) {
            user.setEmail(input.email());
        }
        if (input.fullName() != null) {
            user.setFullName(input.fullName());
        }
        if (input.roleIds() != null) {
            user.setRoles(resolveRoles(input.roleIds()));
        }
        return userRepository.save(user);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument String id) {
        UUID userId = UUID.fromString(id);
        if (!userRepository.existsById(userId)) {
            return false;
        }
        userRepository.deleteById(userId);
        return true;
    }

    @MutationMapping
    public Role createRole(@Argument CreateRoleInput input) {
        Role role = new Role(null, input.name());
        return roleRepository.save(role);
    }

    @MutationMapping
    public Role updateRole(@Argument String id, @Argument UpdateRoleInput input) {
        Role role = roleRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        if (input.name() != null) {
            role.setName(input.name());
        }
        return roleRepository.save(role);
    }

    @MutationMapping
    public Boolean deleteRole(@Argument String id) {
        UUID roleId = UUID.fromString(id);
        if (!roleRepository.existsById(roleId)) {
            return false;
        }
        roleRepository.deleteById(roleId);
        return true;
    }

    private Set<Role> resolveRoles(List<String> roleIds) {
        List<UUID> ids = roleIds.stream().map(UUID::fromString).toList();
        List<Role> roles = roleRepository.findAllById(ids);
        if (roles.size() != ids.size()) {
            throw new IllegalArgumentException("One or more roles not found");
        }
        return new HashSet<>(roles);
    }

    public record CreateUserInput(
            String username,
            String email,
            String fullName,
            List<String> roleIds
    ) {
    }

    public record UpdateUserInput(
            String username,
            String email,
            String fullName,
            List<String> roleIds
    ) {
    }

    public record CreateRoleInput(String name) {
    }

    public record UpdateRoleInput(String name) {
    }
}
