package com.practicasDeDesarrollo.backend.unit.config;

import com.practicasDeDesarrollo.backend.config.LoggingFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoggingFilter — Unit Tests")
class LoggingFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        MDC.clear();
        SecurityContextHolder.clearContext();
        lenient().when(request.getMethod()).thenReturn("GET");
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Test
    @DisplayName("setea requestId, method, uri en MDC antes del chain")
    void setsMdcBeforeFilter() throws Exception {
        LoggingFilter filter = new LoggingFilter();
        filter.doFilter(request, response, (req, res) -> {
            assertNotNull(MDC.get("requestId"));
            assertEquals("GET", MDC.get("method"));
            assertEquals("/api/v1/test", MDC.get("uri"));
            filterChain.doFilter(req, res);
        });
    }

    @Test
    @DisplayName("agrega userId al MDC cuando hay usuario autenticado")
    void addsUserIdWhenAuthenticated() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user@test.com");
        when(auth.getPrincipal()).thenReturn("user@test.com");
        SecurityContextHolder.getContext().setAuthentication(auth);
        LoggingFilter filter = new LoggingFilter();

        filter.doFilter(request, response, filterChain);
        verify(auth).isAuthenticated();
        verify(auth).getName();
        verify(auth, atLeastOnce()).getPrincipal();
    }

    @Test
    @DisplayName("no agrega userId cuando no hay autenticación")
    void noUserIdWhenAnonymous() throws Exception {
        LoggingFilter filter = new LoggingFilter();
        filter.doFilter(request, response, filterChain);
        assertNull(MDC.get("userId"));
    }

    @Test
    @DisplayName("no agrega userId cuando es anonymousUser")
    void noUserIdForAnonymousUser() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(auth);
        LoggingFilter filter = new LoggingFilter();

        filter.doFilter(request, response, filterChain);
        verify(auth).isAuthenticated();
        verify(auth, atLeastOnce()).getPrincipal();
    }

    @Test
    @DisplayName("consulta statusCode después del chain")
    void queriesStatusAfterFilter() throws Exception {
        when(response.getStatus()).thenReturn(200);
        LoggingFilter filter = new LoggingFilter();
        filter.doFilter(request, response, filterChain);
        verify(response, atLeastOnce()).getStatus();
    }

    @Test
    @DisplayName("limpia MDC en el finally")
    void clearsMdcInFinally() throws Exception {
        LoggingFilter filter = new LoggingFilter();
        filter.doFilter(request, response, filterChain);
        assertNull(MDC.get("requestId"));
        assertNull(MDC.get("method"));
        assertNull(MDC.get("uri"));
    }

    @Test
    @DisplayName("llama al filterChain")
    void invokesFilterChain() throws Exception {
        LoggingFilter filter = new LoggingFilter();
        filter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }
}
