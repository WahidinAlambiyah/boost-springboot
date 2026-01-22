package com.alambiyah.service;

import com.alambiyah.app.User;
import com.alambiyah.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).getContent();
    }

    public User findUser(String id) {
        return userRepository.findById(UUID.fromString(id)).orElse(null);
    }
}
