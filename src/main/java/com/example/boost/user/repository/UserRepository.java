package com.example.boost.user.repository;

import com.example.boost.user.model.Role;
import com.example.boost.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);
}
