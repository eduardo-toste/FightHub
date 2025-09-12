package com.fighthub.config;

import com.fighthub.repository.UsuarioRepository;
import com.fighthub.security.SecurityFilter;
import com.fighthub.service.JwtService;
import com.fighthub.utils.ErrorWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.util.Map;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http,
                                                       JwtService jwtService,
                                                       UsuarioRepository usuarioRepository,
                                                       ErrorWriter errorWriter,
                                                       ObjectMapper objectMapper) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(restAccessDeniedHandler(objectMapper)))
                .addFilterBefore(new SecurityFilter(jwtService, usuarioRepository, errorWriter),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType("application/json");
            try (PrintWriter writer = response.getWriter()) {
                writer.write(objectMapper.writeValueAsString(Map.of(
                        "timestamp", OffsetDateTime.now().toString(),
                        "status", 401,
                        "error", "Unauthorized",
                        "message", "Token ausente ou inválido",
                        "path", request.getRequestURI()
                )));
            }
        };
    }

    @Bean
    public AccessDeniedHandler restAccessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, accessDeniedException) -> {
            response.setStatus(403);
            response.setContentType("application/json");
            try (PrintWriter writer = response.getWriter()) {
                writer.write(objectMapper.writeValueAsString(Map.of(
                        "timestamp", OffsetDateTime.now().toString(),
                        "status", 403,
                        "error", "Forbidden",
                        "message", "Acesso negado",
                        "path", request.getRequestURI()
                )));
            }
        };
    }
}