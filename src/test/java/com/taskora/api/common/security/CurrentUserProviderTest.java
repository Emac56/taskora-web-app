package com.taskora.api.common.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CurrentUserProviderTest {

    private final CurrentUserProvider currentUserProvider =
            new CurrentUserProvider();

    @Mock
    private SecurityContext securityContext;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @BeforeEach
    void setUp() {
        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        securityContextHolderMock.when(SecurityContextHolder::getContext)
                .thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        securityContextHolderMock.close();
    }

    @Test
    void shouldReturnFalseWhenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertFalse(currentUserProvider.isAdmin());
    }

    @Test
    void shouldReturnFalseWhenNotAuthenticated() {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken("user", "pass");
        authentication.setAuthenticated(false);

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        assertFalse(currentUserProvider.isAdmin());
    }

    @Test
    void shouldReturnFalseWhenAuthenticatedWithoutAdminRole() {
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_USER"));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "user", "pass", authorities);

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        assertFalse(currentUserProvider.isAdmin());
    }

    @Test
    void shouldReturnTrueWhenAuthenticatedWithAdminRole() {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN"));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin", "pass", authorities);

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        assertTrue(currentUserProvider.isAdmin());
    }
}
