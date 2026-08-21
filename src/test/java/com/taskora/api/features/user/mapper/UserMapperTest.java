package com.taskora.api.features.user.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.taskora.api.common.enums.Role;
import com.taskora.api.features.user.dto.response.LoginResponse;
import com.taskora.api.features.user.entity.User;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void shouldMapUserToLoginResponse() {
        User user = new User();

        user.setId(1L);
        user.setName("Admin");
        user.setEmail("admin@taskora.com");
        user.setRole(Role.ADMIN);

        LoginResponse response = userMapper.toLoginResponse(user);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Admin", response.getName());
        assertEquals("admin@taskora.com", response.getEmail());
        assertEquals(Role.ADMIN, response.getRole());
    }
}