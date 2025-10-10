package com.fighthub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fighthub.config.TestSecurityConfig;
import com.fighthub.dto.auth.*;
import com.fighthub.exception.GlobalExceptionHandler;
import com.fighthub.exception.TokenInvalidoException;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.repository.TokenRepository;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@Import({ TestSecurityConfig.class, GlobalExceptionHandler.class })
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private JwtService jwtService;
    @MockBean private UsuarioRepository usuarioRepository;
    @MockBean private TokenRepository tokenRepository;
    @MockBean private ErrorWriter errorWriter;

    @Test
    void deveFazerLoginComSucesso() throws Exception {
        var request = new AuthRequest("teste@email.com", "123456");
        var response = new AuthResponse("access-token", "refresh-token");

        when(authService.login(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void deveAtualizarTokenComSucesso() throws Exception {
        String refreshToken = "refresh-valido";
        var response = new RefreshTokenResponse("novo-access-token");

        when(authService.atualizarToken(refreshToken)).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newAccessToken").value("novo-access-token"));
    }

    @Test
    void deveRetornar401_QuandoRefreshTokenForInvalido() throws Exception {
        String refreshToken = "refresh-invalido";
        when(authService.atualizarToken(refreshToken)).thenThrow(new TokenInvalidoException());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRealizarLogoutComSucesso() throws Exception {
        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer token-qualquer"))
                .andExpect(status().isOk());
    }

    @Test
    void deveEnviarEmailRecuperacaoComSucesso() throws Exception {
        var request = new RecuperarSenhaRequest("email@teste.com");
        doNothing().when(authService).recoverPassword(any());

        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar404_RecuperarSenhaUsuarioInexistente() throws Exception {
        var request = new RecuperarSenhaRequest("naoexiste@teste.com");
        doThrow(new UsuarioNaoEncontradoException()).when(authService).recoverPassword(any());

        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveValidarCodigoComSucesso() throws Exception {
        var request = new ValidarCodigoRecuperacaoRequest("email@teste.com", "123456");
        doNothing().when(authService).validateRecoverCode(any());

        mockMvc.perform(post("/auth/recuperar-senha/validar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar401_QuandoCodigoRecuperacaoForInvalido() throws Exception {
        var request = new ValidarCodigoRecuperacaoRequest("email@teste.com", "codigo-invalido");

        doThrow(new TokenInvalidoException()).when(authService).validateRecoverCode(any());

        mockMvc.perform(post("/auth/recuperar-senha/validar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveCriarNovaSenhaComSucesso() throws Exception {
        var request = new ConfirmarRecuperacaoSenhaRequest("123456", "email@teste.com", "novaSenhaSegura123");
        doNothing().when(authService).confirmarRecuperacaoSenha(any());

        mockMvc.perform(post("/auth/recuperar-senha/nova-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar401_QuandoCriarNovaSenhaComCodigoInvalido() throws Exception {
        var request = new ConfirmarRecuperacaoSenhaRequest("codigo-invalido", "email@teste.com", "novaSenha");

        doThrow(new TokenInvalidoException()).when(authService).confirmarRecuperacaoSenha(any());

        mockMvc.perform(post("/auth/recuperar-senha/nova-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}