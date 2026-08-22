package com.taskora.api.common.config;

import java.io.IOException;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Forces the CsrfToken to be resolved on every request and writes it to both
 * the XSRF-TOKEN cookie (via CookieCsrfTokenRepository) and the X-XSRF-TOKEN
 * response header.
 *
 * Exposing the token as an HTTP response header allows cross-origin SPAs
 * (such as frontend on Vercel, backend on Render) to read the token value
 * without violating the browser Same-Origin Policy on cookies.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken == null) {
            csrfToken = (CsrfToken) request.getAttribute("_csrf");
        }

        if (csrfToken != null) {
            // Accessing .getToken() forces deferred token resolution
            String token = csrfToken.getToken();
            if (csrfToken.getHeaderName() != null && token != null) {
                response.setHeader(csrfToken.getHeaderName(), token);
            }
        }

        filterChain.doFilter(request, response);
    }
}
