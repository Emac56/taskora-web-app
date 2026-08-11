package com.taskora.api.features.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskora.api.common.ratelimit.LoginRateLimiter;
import com.taskora.api.common.util.ClientIpResolver;
import com.taskora.api.features.user.dto.request.LoginRequest;
import com.taskora.api.features.user.dto.response.LoginResponse;
import com.taskora.api.features.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;
    private final LoginRateLimiter loginRateLimiter;
    private final ClientIpResolver clientIpResolver;

    public UserController(
            UserService userService,
            SecurityContextRepository securityContextRepository,
            LoginRateLimiter loginRateLimiter,
            ClientIpResolver clientIpResolver) {
        this.userService = userService;
        this.securityContextRepository = securityContextRepository;
        this.loginRateLimiter = loginRateLimiter;
        this.clientIpResolver = clientIpResolver;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String clientId = clientIpResolver.resolve(httpRequest);
        loginRateLimiter.checkAllowed(clientId);

        LoginResponse response = userService.login(request);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                response.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + response.getRole().name())));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return ResponseEntity.ok(response);
    }
}