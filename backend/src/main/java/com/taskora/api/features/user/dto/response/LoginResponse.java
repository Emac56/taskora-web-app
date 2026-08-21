package com.taskora.api.features.user.dto.response;

import com.taskora.api.common.enums.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private Long id;

    private String name;

    private String email;

    private Role role;

}