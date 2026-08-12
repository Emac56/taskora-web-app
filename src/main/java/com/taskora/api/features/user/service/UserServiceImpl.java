package com.taskora.api.features.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.taskora.api.features.user.dto.request.LoginRequest;
import com.taskora.api.features.user.dto.response.LoginResponse;
import com.taskora.api.features.user.entity.User;
import com.taskora.api.features.user.mapper.UserMapper;
import com.taskora.api.features.user.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private static final String DUMMY_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoOa2Fc.wG.5vT.J0y.f.gK5n8V1i8XZ8W";
            
    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        String hashToCheck = (user != null) ? user.getPassword() : DUMMY_HASH;
        boolean passwordMatches =
                passwordEncoder.matches(request.getPassword(), hashToCheck);

        if (user == null || !passwordMatches) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        return userMapper.toLoginResponse(user);
    }
}