package com.taskora.api.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ClientIpResolverTest {

    @Test
    void shouldReturnRemoteAddrWhenProxyHeadersNotTrusted() {
        ClientIpResolver resolver = new ClientIpResolver(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.10");
        request.addHeader("X-Forwarded-For", "198.51.100.5");

        String result = resolver.resolve(request);

        assertEquals("203.0.113.10", result);
    }

    @Test
    void shouldReturnFirstForwardedIpWhenProxyHeadersTrusted() {
        ClientIpResolver resolver = new ClientIpResolver(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.10");
        request.addHeader("X-Forwarded-For", "198.51.100.5, 10.0.0.1");

        String result = resolver.resolve(request);

        assertEquals("198.51.100.5", result);
    }

    @Test
    void shouldFallBackToRemoteAddrWhenProxyHeadersTrustedButHeaderMissing() {
        ClientIpResolver resolver = new ClientIpResolver(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.10");

        String result = resolver.resolve(request);

        assertEquals("203.0.113.10", result);
    }

    @Test
    void shouldFallBackToRemoteAddrWhenForwardedHeaderIsBlank() {
        ClientIpResolver resolver = new ClientIpResolver(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.10");
        request.addHeader("X-Forwarded-For", "   ");

        String result = resolver.resolve(request);

        assertEquals("203.0.113.10", result);
    }
}