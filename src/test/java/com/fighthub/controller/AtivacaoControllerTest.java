package com.fighthub.controller;

import com.fighthub.config.TestSecurityConfig;
import com.fighthub.dto.auth.AtivacaoRequest;
import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.exception.TokenExpiradoException;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.utils.ControllerTestBase;
import com.fighthub.service.AtivacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AtivacaoController.class)
@Import(TestSecurityConfig.class)
class AtivacaoControllerTest extends ControllerTestBase {

    @MockBean
    private AtivacaoService ativacaoService;

    private AtivacaoRequest criarRequestValido() {
        EnderecoRequest endereco = new EnderecoRequest(
                "12345-678",
                "Rua das Flores",
                "123",
                "Apto 45",
                "Centro",
                "São Paulo",
                "SP"
        );

        return new AtivacaoRequest(
                "token-valido",
                "SenhaForte123",
                "(11)91234-5678",
                endereco
        );
    }

    @Test
    void deveAtivarContaComSucesso() throws Exception {
        AtivacaoRequest request = criarRequestValido();

        doNothing().when(ativacaoService).ativarConta(request);

        mockMvc.perform(post("/ativar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(ativacaoService).ativarConta(request);
    }

    @Test
    void deveRetornarBadRequest_QuandoRequestInvalido() throws Exception {
        AtivacaoRequest requestInvalido = new AtivacaoRequest(
                "",
                "",
                "",
                null
        );

        mockMvc.perform(post("/ativar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.validationError").isArray());
    }

    @Test
    void deveRetornarUnauthorized_QuandoTokenExpiradoOuInvalido() throws Exception {
        AtivacaoRequest request = criarRequestValido();

        doThrow(new TokenExpiradoException())
                .when(ativacaoService).ativarConta(request);

        mockMvc.perform(post("/ativar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token expirado."));

        verify(ativacaoService).ativarConta(request);
    }

    @Test
    void deveRetornarNotFound_QuandoUsuarioNaoEncontrado() throws Exception {
        AtivacaoRequest request = criarRequestValido();

        doThrow(new UsuarioNaoEncontradoException())
                .when(ativacaoService).ativarConta(request);

        mockMvc.perform(post("/ativar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado."));

        verify(ativacaoService).ativarConta(request);
    }
}