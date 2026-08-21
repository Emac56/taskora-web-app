package com.taskora.api.common.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiErrorResponse {

    private boolean success;

    private String message;
}
