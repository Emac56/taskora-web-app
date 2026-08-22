package com.taskora.api.common.config;

import java.io.IOException;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Forces the CsrfToken to be rendered on every request, which is what
 * actually triggers CookieCsrfTokenRepository to write the XSRF-TOKEN
 * cookie to the response.
 *
 * Without this filter, Spring Security only writes the cookie lazily
 * the first time something reads csrfToken.getToken() — which means an
 * SPA hitting the API for the first time (e.g. on page load) may not
 * receive the cookie until a second request. That breaks the
 * "read cookie in JS, send as header" flow the frontend depends on.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        if (csrfToken != null) {
            // Accessing .getToken() is what forces the deferred token
            // to actually resolve and get saved to the cookie.
            String token = csrfToken.getToken();

            // The frontend (Vercel) can't read the XSRF-TOKEN cookie via
            // document.cookie — it's scoped to this backend's own domain
            // (Render), a different registrable domain. Mirroring the
            // same value onto a response header lets the frontend
            // capture it from the response instead (see http.js). CORS
            // must explicitly expose this header
            // (see SecurityConfig#corsConfigurationSource) or
            // cross-origin JS still won't be able to read it.
            response.setHeader("X-XSRF-TOKEN", token);
        }

        filterChain.doFilter(request, response);
    }
}
