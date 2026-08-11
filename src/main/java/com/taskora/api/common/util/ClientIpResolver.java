package com.taskora.api.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ClientIpResolver {

    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final boolean trustProxyHeaders;

    public ClientIpResolver(
            @Value("${app.security.trust-proxy-headers:false}") boolean trustProxyHeaders) {
        this.trustProxyHeaders = trustProxyHeaders;
    }

    public String resolve(HttpServletRequest request) {
        if (trustProxyHeaders) {
            String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);

            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return forwardedFor.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}