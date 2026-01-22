package com.alambiyah.service;

import com.alambiyah.app.User;
import com.alambiyah.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> listUsers(Pageable pageable, UserFilter filter) {
        return userRepository.findAll(buildSpecification(filter), pageable).getContent();
    }

    public User findUser(String id) {
        return userRepository.findById(UUID.fromString(id)).orElse(null);
    }

    private Specification<User> buildSpecification(UserFilter filter) {
        if (filter == null) {
            return Specification.where(null);
        }
        return Specification.where(containsIgnoreCase("username", filter.username()))
                .and(containsIgnoreCase("email", filter.email()))
                .and(containsIgnoreCase("fullName", filter.fullName()));
    }

    private Specification<User> containsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return Specification.where(null);
        }
        String pattern = "%" + value.trim().toLowerCase() + "%";
        return (root, query, builder) -> builder.like(builder.lower(root.get(field)), pattern);
    }
}
