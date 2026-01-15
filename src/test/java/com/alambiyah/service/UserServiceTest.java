package com.alambiyah.service;

import com.alambiyah.app.User;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {
    @Autowired
    private UserService userService;

    @Test
    void listUsersReturnsSeededUsers() {
        List<User> users = userService.listUsers();

        Assertions.assertEquals(3, users.size());
        Assertions.assertTrue(users.stream().anyMatch(user -> "alice".equals(user.getUsername())));
    }

    @Test
    void findUserReturnsUserById() {
        User user = userService.findUser("11111111-1111-1111-1111-111111111111");

        Assertions.assertNotNull(user);
        Assertions.assertEquals("alice@example.com", user.getEmail());
    }
}
