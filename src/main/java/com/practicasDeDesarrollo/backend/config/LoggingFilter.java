package com.practicasDeDesarrollo.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().replace("-", "");
        MDC.put("requestId", requestId);
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());

        long start = System.currentTimeMillis();

        log.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                MDC.put("userId", auth.getName());
            }

            MDC.put("statusCode", String.valueOf(response.getStatus()));
            MDC.put("duration", String.valueOf(duration));

            if (response.getStatus() >= 500) {
                log.error("Request completed with server error: {} {} -> {} ({}ms)", request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            } else if (response.getStatus() >= 400) {
                log.warn("Request completed with client error: {} {} -> {} ({}ms)", request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            } else {
                log.info("Request completed: {} {} -> {} ({}ms)", request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            }

            MDC.clear();
        }
    }
}
