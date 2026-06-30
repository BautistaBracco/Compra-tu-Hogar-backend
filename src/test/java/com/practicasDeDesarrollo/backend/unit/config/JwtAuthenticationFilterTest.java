package com.practicasDeDesarrollo.backend.unit.config;

import com.practicasDeDesarrollo.backend.config.JwtAuthenticationFilter;
import com.practicasDeDesarrollo.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter — Unit Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Test
    @DisplayName("loguea warning cuando el token es inválido")
    void logsWarningOnInvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.jwt.token");
        when(jwtService.extractUsername("invalid.jwt.token")).thenThrow(new RuntimeException("JWT signature does not match"));

        filter.doFilter(request, response, filterChain);

        verify(jwtService).extractUsername("invalid.jwt.token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("pasa al chain sin loguear si no hay header Authorization")
    void noAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    @DisplayName("pasa al chain sin loguear si el header no es Bearer")
    void notBearerToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic base64token");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }
}
