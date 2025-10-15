package com.fighthub.integration;

import com.fighthub.dto.auth.*;
import com.fighthub.model.Endereco;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.TokenRepository;
import com.fighthub.utils.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthIntegrationTest extends IntegrationTestBase {

    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TokenRepository tokenRepository;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        tokenRepository.deleteAll();
        usuarioRepository.deleteAll();

        Endereco endereco = Endereco.builder()
                .cep("12345-678")
                .logradouro("Rua Exemplo")
                .numero("123")
                .complemento("Apto 45")
                .bairro("Centro")
                .cidade("São Paulo")
                .estado("SP")
                .build();

        usuario = new Usuario(
                UUID.randomUUID(),
                "Teste",
                "teste@gmail.com",
                passwordEncoder.encode("123456"),
                null, // foto
                Role.ALUNO,
                false, // loginSocial
                true,  // ativo
                "123.456.789-00", // cpf
                "(11)91234-5678", // telefone
                endereco
        );
    }

    @Test
    void deveLogarUsuarioComSucesso_QuandoCredenciaisForemValidas() throws Exception {
        // Arrange
        usuarioRepository.save(usuario);

        var request = new AuthRequest("teste@gmail.com", "123456");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void deveNegarLogin_QuandoCredenciaisInvalidas() throws Exception {
        var request = new AuthRequest("inexistente@email.com", "senha-errada");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveAtualizarToken_QuandoRefreshTokenValido() throws Exception {
        // Arrange
        usuarioRepository.save(usuario);
        
        // Fazer login para obter refresh token
        var loginRequest = new AuthRequest("teste@gmail.com", "123456");
        var loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        var loginResult = objectMapper.readTree(loginResponse.getResponse().getContentAsString());
        String refreshToken = loginResult.get("refreshToken").asText();
        
        var refreshRequest = new RefreshTokenRequest(refreshToken);

        // Act & Assert
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newAccessToken").isNotEmpty());
    }

    @Test
    void deveNegarRefresh_QuandoTokenInvalido() throws Exception {
        var request = new RefreshTokenRequest("token-invalido");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveFazerLogout_QuandoTokenValido() throws Exception {
        // Arrange
        usuarioRepository.save(usuario);
        
        var loginRequest = new AuthRequest("teste@gmail.com", "123456");
        var loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        var loginResult = objectMapper.readTree(loginResponse.getResponse().getContentAsString());
        String accessToken = loginResult.get("accessToken").asText();

        // Act & Assert
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deveRecuperarSenha_QuandoEmailValido() throws Exception {
        // Arrange
        usuarioRepository.save(usuario);
        var request = new RecuperarSenhaRequest("teste@gmail.com");

        // Act & Assert
        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deveNegarRecuperacao_QuandoEmailInvalido() throws Exception {
        var request = new RecuperarSenhaRequest("email@inexistente.com");

        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}