package com.fighthub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fighthub.config.TestSecurityConfig;
import com.fighthub.dto.auth.AtivacaoRequest;
import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.repository.TokenRepository;
import com.fighthub.repository.UsuarioRepository;
import com.fighthub.service.AtivacaoService;
import com.fighthub.service.JwtService;
import com.fighthub.utils.ErrorWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AtivacaoController.class)
@Import(TestSecurityConfig.class)
class AtivacaoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AtivacaoService ativacaoService;
    @MockBean private JwtService jwtService;
    @MockBean private UsuarioRepository usuarioRepository;
    @MockBean private TokenRepository tokenRepository;
    @MockBean private ErrorWriter errorWriter;

    @BeforeEach
    void setupSecurity() {
        when(jwtService.tokenValido(any())).thenReturn(true);
        when(jwtService.extrairEmail(any())).thenReturn("usuario@teste.com");
        when(usuarioRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.empty());
        when(tokenRepository.findByTokenAndExpiredFalseAndRevokedFalse(any()))
                .thenReturn(Optional.of(new com.fighthub.model.Token()));
    }

    @Test
    void deveAtivarContaComSucesso() throws Exception {
        var endereco = new EnderecoRequest(
                "12345-678",
                "Rua das Flores",
                "123",
                "Apto 45",
                "Centro",
                "São Paulo",
                "SP"
        );

        var request = new AtivacaoRequest(
                "token-de-ativacao",
                "senhaSegura123",
                "(11)12345-6789",
                endereco
        );

        doNothing().when(ativacaoService).ativarConta(any(AtivacaoRequest.class));

        mockMvc.perform(post("/ativar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}