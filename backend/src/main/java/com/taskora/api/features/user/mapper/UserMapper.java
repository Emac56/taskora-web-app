package com.taskora.api.features.user.mapper;

import org.springframework.stereotype.Component;

import com.taskora.api.features.user.dto.response.LoginResponse;
import com.taskora.api.features.user.entity.User;

@Component
public class UserMapper {

    public LoginResponse toLoginResponse(User user) {

        LoginResponse response = new LoginResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        return response;
    }
}