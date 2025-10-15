package com.fighthub.controller;

import com.fighthub.dto.auth.*;
import com.fighthub.exception.GlobalExceptionHandler;
import com.fighthub.exception.TokenInvalidoException;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.service.AuthService;
import com.fighthub.utils.ControllerTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest extends ControllerTestBase {

    @MockBean private AuthService authService;

    @Test
    void deveFazerLoginComSucesso() throws Exception {
        var request = new AuthRequest("usuario@email.com", "senha123");
        var response = new AuthResponse("access-token", "refresh-token");

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).login(any());
    }

    @Test
    void deveAtualizarTokenComSucesso() throws Exception {
        String refreshToken = "refresh-token";
        var response = new RefreshTokenResponse("novo-access-token");

        when(authService.atualizarToken(refreshToken)).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"refreshToken": "refresh-token"}
                                 """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newAccessToken").value("novo-access-token"));

        verify(authService).atualizarToken(refreshToken);
    }

    @Test
    void deveRetornarUnauthorized_QuandoRefreshTokenForInvalido() throws Exception {
        when(authService.atualizarToken("token-invalido")).thenThrow(new TokenInvalidoException());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "token-invalido"}
                                """))
                .andExpect(status().isUnauthorized());

        verify(authService).atualizarToken("token-invalido");
    }

    @Test
    void deveRealizarLogoutComSucesso() throws Exception {
        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer qualquer-token"))
                .andExpect(status().isOk());

        verify(authService).logout(any());
    }

    @Test
    void deveEnviarEmailRecuperacaoComSucesso() throws Exception {
        var request = new RecuperarSenhaRequest("usuario@email.com");

        doNothing().when(authService).recoverPassword(any());

        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).recoverPassword(any());
    }

    @Test
    void deveRetornarNotFound_QuandoEmailNaoExistirEmRecuperacao() throws Exception {
        var request = new RecuperarSenhaRequest("inexistente@email.com");

        doThrow(new UsuarioNaoEncontradoException()).when(authService).recoverPassword(any());

        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado."));

        verify(authService).recoverPassword(any());
    }

    @Test
    void deveValidarCodigoComSucesso() throws Exception {
        var request = new ValidarCodigoRecuperacaoRequest("usuario@email.com", "123456");

        doNothing().when(authService).validateRecoverCode(any());

        mockMvc.perform(post("/auth/recuperar-senha/validar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).validateRecoverCode(any());
    }

    @Test
    void deveRetornarUnauthorized_QuandoCodigoRecuperacaoInvalido() throws Exception {
        var request = new ValidarCodigoRecuperacaoRequest("usuario@email.com", "codigo-invalido");

        doThrow(new TokenInvalidoException()).when(authService).validateRecoverCode(any());

        mockMvc.perform(post("/auth/recuperar-senha/validar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authService).validateRecoverCode(any());
    }

    @Test
    void deveCriarNovaSenhaComSucesso() throws Exception {
        var request = new ConfirmarRecuperacaoSenhaRequest("123456", "usuario@email.com", "NovaSenha123");

        doNothing().when(authService).confirmarRecuperacaoSenha(any());

        mockMvc.perform(post("/auth/recuperar-senha/nova-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).confirmarRecuperacaoSenha(any());
    }

    @Test
    void deveRetornarUnauthorized_QuandoNovaSenhaUsarCodigoInvalido() throws Exception {
        var request = new ConfirmarRecuperacaoSenhaRequest("codigo-invalido", "usuario@email.com", "NovaSenha123");

        doThrow(new TokenInvalidoException()).when(authService).confirmarRecuperacaoSenha(any());

        mockMvc.perform(post("/auth/recuperar-senha/nova-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authService).confirmarRecuperacaoSenha(any());
    }
}