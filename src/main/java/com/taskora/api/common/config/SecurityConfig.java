package com.taskora.api.common.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskora.api.common.dto.response.ApiErrorResponse;

@Configuration
public class SecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(
                List.of("Content-Type", "Authorization", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            CorsConfigurationSource corsConfigurationSource) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                    // No session exists yet at login time, so there's nothing
                    // for the CSRF cookie to protect on this specific request.
                    // Brute-force / automated login attempts are covered by
                    // LoginRateLimiter instead.
                    .ignoringRequestMatchers("/api/v1/users/login"))
            .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .securityContext(context -> context
                    .securityContextRepository(securityContextRepository))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/users/login")
                        .permitAll()
                    .requestMatchers(HttpMethod.GET,
                            "/api/v1/tutorials",
                            "/api/v1/tutorials/**",
                            "/api/v1/tutorial-steps/**")
                        .permitAll()
                    .requestMatchers(HttpMethod.POST,
        "/api/v1/tutorials",
        "/api/v1/tutorials/*/steps",
        "/api/v1/tutorial-steps/images")
    .hasRole(ROLE_ADMIN)
                        .hasRole(ROLE_ADMIN)
                    .requestMatchers(HttpMethod.PUT,
                            "/api/v1/tutorials/*",
                            "/api/v1/tutorial-steps/*")
                        .hasRole(ROLE_ADMIN)
                    .requestMatchers(HttpMethod.DELETE,
                            "/api/v1/tutorials/*",
                            "/api/v1/tutorial-steps/*")
                        .hasRole(ROLE_ADMIN)
                    .anyRequest().authenticated())
            .exceptionHandling(exception -> exception


.authenticationEntryPoint((request, response, authException) -> {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");

    ApiErrorResponse errorResponse = new ApiErrorResponse();
    errorResponse.setSuccess(false);
    errorResponse.setMessage("Authentication required.");

    new ObjectMapper().writeValue(response.getWriter(), errorResponse);
}))
            .formLogin(AbstractHttpConfigurer::disable)

            .logout(logout -> logout
                    .logoutUrl("/api/v1/users/logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessHandler((request, response, authentication) ->
                            response.setStatus(HttpStatus.NO_CONTENT.value())));

        return http.build();
    }
}
