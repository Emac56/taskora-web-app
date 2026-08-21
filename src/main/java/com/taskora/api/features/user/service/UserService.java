package com.taskora.api.features.user.service;

import com.taskora.api.features.user.dto.request.LoginRequest;
import com.taskora.api.features.user.dto.response.LoginResponse;

public interface UserService {

    LoginResponse login(LoginRequest request);
}