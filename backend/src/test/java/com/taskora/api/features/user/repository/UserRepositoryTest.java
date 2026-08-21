package com.taskora.api.features.user.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.taskora.api.common.enums.Role;
import com.taskora.api.features.user.entity.User;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByEmail() {
        User user = new User();

        user.setName("Admin");
        user.setEmail("admin@taskora.com");
        user.setPassword("password");
        user.setRole(Role.ADMIN);

        userRepository.save(user);

        Optional<User> result =
                userRepository.findByEmail("admin@taskora.com");

        assertTrue(result.isPresent());
        assertEquals("Admin", result.get().getName());
        assertEquals("admin@taskora.com", result.get().getEmail());
        assertEquals(Role.ADMIN, result.get().getRole());
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        Optional<User> result =
                userRepository.findByEmail("notfound@taskora.com");

        assertTrue(result.isEmpty());
    }
}