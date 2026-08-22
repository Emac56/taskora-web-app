package com.taskora.api.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.DefaultCsrfToken;

/**
 * Covers the fix for BE-146: the frontend (Vercel) can't read the
 * XSRF-TOKEN cookie via document.cookie since it's scoped to the
 * backend's own domain (Render). CsrfCookieFilter now also mirrors the
 * resolved token onto a response header the frontend can read instead.
 */
class CsrfCookieFilterTest {

    private final CsrfCookieFilter filter = new CsrfCookieFilter();

    @Test
    void shouldMirrorCsrfTokenOntoResponseHeaderWhenTokenPresent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("_csrf",
                new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "test-token-value"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals("test-token-value", response.getHeader("X-XSRF-TOKEN"));
    }

    @Test
    void shouldNotSetHeaderWhenNoCsrfTokenOnRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertNull(response.getHeader("X-XSRF-TOKEN"));
    }

    @Test
    void shouldStillContinueFilterChainRegardlessOfCsrfTokenPresence() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertSame(request, chain.getRequest());
        assertSame(response, chain.getResponse());
    }
}
