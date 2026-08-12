package com.taskora.api.features.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.taskora.api.common.enums.Role;
import com.taskora.api.features.user.dto.request.LoginRequest;
import com.taskora.api.features.user.dto.response.LoginResponse;
import com.taskora.api.features.user.entity.User;
import com.taskora.api.features.user.mapper.UserMapper;
import com.taskora.api.features.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

  
  @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString()))
                .thenReturn("dummy-hash-value");

        user = new User();
        user.setId(1L);
        user.setName("Admin");
        user.setEmail("admin@taskora.com");
        user.setPassword("encoded-password");
        user.setRole(Role.ADMIN);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@taskora.com");
        loginRequest.setPassword("password");

        loginResponse = new LoginResponse();
        loginResponse.setId(1L);
        loginResponse.setName("Admin");
        loginResponse.setEmail("admin@taskora.com");
        loginResponse.setRole(Role.ADMIN);
    }
    
    @Test
    void shouldLoginSuccessfullyWhenUserExists() {
        when(userRepository.findByEmail("admin@taskora.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "encoded-password"))
                .thenReturn(true);

        when(userMapper.toLoginResponse(user))
                .thenReturn(loginResponse);

        LoginResponse result = userService.login(loginRequest);

        assertEquals(loginResponse, result);

        verify(userRepository).findByEmail("admin@taskora.com");
        verify(passwordEncoder).matches("password", "encoded-password");
        verify(userMapper).toLoginResponse(user);
    }

  @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.findByEmail("admin@taskora.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.matches(eq("password"), anyString()))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(loginRequest)
        );

        verify(userRepository).findByEmail("admin@taskora.com");
        verify(passwordEncoder).matches(eq("password"), anyString());
    }
    
    @Test
    void shouldStillCallPasswordEncoderWhenUserDoesNotExist() {
        when(userRepository.findByEmail("admin@taskora.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(loginRequest)
        );

        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        when(userRepository.findByEmail("admin@taskora.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "encoded-password"))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(loginRequest)
        );

        verify(userRepository).findByEmail("admin@taskora.com");
        verify(passwordEncoder).matches("password", "encoded-password");
    }
    
    @Test
    void shouldStillCallPasswordEncoderWhenUserDoesNotExist() {
        when(userRepository.findByEmail("admin@taskora.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(loginRequest)
        );

        verify(passwordEncoder).matches(anyString(), anyString());
    }
}