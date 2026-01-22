package com.alambiyah.graphql;

import com.alambiyah.app.Role;
import com.alambiyah.app.User;
import com.alambiyah.repository.RoleRepository;
import com.alambiyah.repository.UserRepository;
import com.alambiyah.service.UserFilter;
import com.alambiyah.service.UserService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
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
    public List<User> users(
            @Argument Integer page,
            @Argument Integer size,
            @Argument String sortBy,
            @Argument SortDirection sortDirection,
            @Argument UserFilterInput filter
    ) {
        return userService.listUsers(
                resolvePageable(page, size, sortBy, sortDirection, "username", Set.of("username", "email", "fullName")),
                toUserFilter(filter)
        );
    }

    @QueryMapping
    public User user(@Argument String id) {
        return userService.findUser(id);
    }

    @SchemaMapping(typeName = "User", field = "roles")
    public Set<Role> roles(User user) {
        if (user.getId() == null) {
            return Set.of();
        }
        return userRepository.findRolesByUserId(user.getId());
    }

    @QueryMapping
    public List<Role> roles(
            @Argument Integer page,
            @Argument Integer size,
            @Argument String sortBy,
            @Argument SortDirection sortDirection,
            @Argument RoleFilter filter
    ) {
        return roleRepository.findAll(
                buildRoleSpecification(filter),
                resolvePageable(page, size, sortBy, sortDirection, "name", Set.of("name"))
        ).getContent();
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

    private Pageable resolvePageable(
            Integer page,
            Integer size,
            String sortBy,
            SortDirection sortDirection,
            String defaultSort,
            Set<String> allowedSorts
    ) {
        int pageNumber = page == null ? 0 : Math.max(page, 0);
        int pageSize = size == null ? 20 : Math.max(size, 1);
        String sortField = resolveSortField(sortBy, defaultSort, allowedSorts);
        Sort.Direction direction = resolveSortDirection(sortDirection);
        return PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
    }

    private Sort.Direction resolveSortDirection(SortDirection sortDirection) {
        if (sortDirection == null) {
            return Sort.Direction.ASC;
        }
        return sortDirection == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private String resolveSortField(String sortBy, String defaultSort, Set<String> allowedSorts) {
        String field = (sortBy == null || sortBy.isBlank()) ? defaultSort : sortBy;
        if (!allowedSorts.contains(field)) {
            throw new IllegalArgumentException("Unsupported sort field: " + field);
        }
        return field;
    }

    private Specification<Role> buildRoleSpecification(RoleFilter filter) {
        if (filter == null) {
            return Specification.where(null);
        }
        return containsIgnoreCase("name", filter.name());
    }

    private Specification<Role> containsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return Specification.where(null);
        }
        String pattern = "%" + value.trim().toLowerCase() + "%";
        return (root, query, builder) -> builder.like(builder.lower(root.get(field)), pattern);
    }

    private UserFilter toUserFilter(UserFilterInput filter) {
        if (filter == null) {
            return null;
        }
        return new UserFilter(filter.username(), filter.email(), filter.fullName());
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

    public record UserFilterInput(String username, String email, String fullName) {
    }

    public record RoleFilter(String name) {
    }
}
