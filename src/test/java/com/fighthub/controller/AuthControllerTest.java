package com.fighthub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fighthub.config.TestSecurityConfig;
import com.fighthub.dto.auth.AuthRequest;
import com.fighthub.dto.auth.AuthResponse;
import com.fighthub.dto.auth.RefreshTokenResponse;
import com.fighthub.exception.TokenInvalidoException;
import com.fighthub.repository.UsuarioRepository;
import com.fighthub.service.AuthService;
import com.fighthub.service.JwtService;
import com.fighthub.utils.ErrorWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AuthService authService;
    @MockBean private JwtService jwtService;
    @MockBean private UsuarioRepository usuarioRepository;
    @MockBean private ErrorWriter errorWriter;

    @Test
    void deveFazerLoginComSucesso() throws Exception {
        // Arrange
        var request = new AuthRequest("teste@email.com", "123456");
        var response = new AuthResponse("access-token", "refresh-token");

        when(authService.login(any(AuthRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void deveAtualizarTokenComSucesso() throws Exception {
        // Arrange
        String refreshToken = "refresh-valido";
        var response = new RefreshTokenResponse("novo-access-token");

        when(authService.atualizarToken(refreshToken)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newAccessToken").value("novo-access-token"));
    }

    @Test
    void deveRetornar401_QuandoRefreshTokenForInvalido() throws Exception {
        // Arrange
        String refreshToken = "refresh-invalido";
        when(authService.atualizarToken(refreshToken)).thenThrow(new TokenInvalidoException());

        // Act & Assert
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRealizarLogoutComSucesso() throws Exception {
        // Arrange
        doNothing().when(authService).logout(any());

        // Act & Assert
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer token-qualquer"))
                .andExpect(status().isOk());
    }
}