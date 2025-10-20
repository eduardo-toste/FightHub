package com.fighthub.integration;

import com.fighthub.dto.auth.AtivacaoRequest;
import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.model.Aluno;
import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.model.enums.TokenType;
import com.fighthub.service.JwtService;
import com.fighthub.utils.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AtivacaoIntegrationTest extends IntegrationTestBase {

    @Autowired private JwtService jwtService;

    private Usuario usuario;
    private Token tokenAtivacao;

    @BeforeEach
    void setup() {
        usuario = usuarioRepository.save(
                Usuario.builder()
                        .nome("Usuário Teste")
                        .email("usuario@email.com")
                        .senha(null)
                        .foto(null)
                        .role(Role.ALUNO)
                        .loginSocial(false)
                        .ativo(false)
                        .telefone("(11)98888-0000")
                        .cpf("11122233344")
                        .endereco(null)
                        .build()
        );

        alunoRepository.save(
                Aluno.builder()
                        .usuario(usuario)
                        .matriculaAtiva(false)
                        .dataNascimento(LocalDate.of(2000, 1, 1))
                        .dataMatricula(LocalDate.now())
                        .build()
        );

        String jwt = jwtService.gerarTokenAtivacao(usuario);

        tokenAtivacao = tokenRepository.save(
                Token.builder()
                        .token(jwt)
                        .tokenType(TokenType.ATIVACAO)
                        .usuario(usuario)
                        .criadoEm(LocalDateTime.now())
                        .expiraEm(LocalDateTime.now().plusHours(1))
                        .expired(false)
                        .revoked(false)
                        .build()
        );
    }

    @Test
    void deveAtivarContaComTokenValido() throws Exception {
        var endereco = new EnderecoRequest("12345-678", "Rua A", "100", null, "Centro", "São Paulo", "SP");
        var request = new AtivacaoRequest(tokenAtivacao.getToken(), "NovaSenha123", "(11)99999-0000", endereco);

        mockMvc.perform(post("/ativar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deveNegarAtivacao_QuandoTokenExpirado() throws Exception {
        var tokenExpirado = Token.builder()
                .token(UUID.randomUUID().toString())
                .tokenType(TokenType.ATIVACAO)
                .usuario(usuario)
                .criadoEm(LocalDateTime.now().minusDays(1))
                .expiraEm(LocalDateTime.now().minusHours(1))
                .expired(true)
                .revoked(false)
                .build();
        tokenRepository.saveAndFlush(tokenExpirado);

        var request = new AtivacaoRequest(
                tokenExpirado.getToken(),
                "SenhaValida123",
                "(11)90000-0000",
                new EnderecoRequest("00000-000", "Rua", "123", null, "Bairro", "Cidade", "SP")
        );

        mockMvc.perform(post("/ativar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Token expirado ou revogado"));
    }

    @Test
    void deveNegarAtivacao_QuandoDadosInvalidos() throws Exception {
        var request = new AtivacaoRequest(
                tokenAtivacao.getToken(),
                "",
                "",
                null
        );

        mockMvc.perform(post("/ativar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationError").isArray());
    }

    @Test
    void deveRetornar401_QuandoUsuarioNaoExisteParaToken() throws Exception {
        var usuarioFalso = Usuario.builder()
                .nome("Usuário Falso")
                .email("usuariofalso@email.com")
                .senha(null)
                .foto(null)
                .role(Role.ALUNO)
                .loginSocial(false)
                .ativo(false)
                .telefone("(11)97777-0000")
                .cpf("99988877766")
                .endereco(null)
                .build();
        usuarioFalso = usuarioRepository.saveAndFlush(usuarioFalso);

        var outroToken = Token.builder()
                .token(UUID.randomUUID().toString())
                .tokenType(TokenType.ATIVACAO)
                .usuario(usuarioFalso)
                .criadoEm(LocalDateTime.now())
                .expiraEm(LocalDateTime.now().plusHours(1))
                .expired(false)
                .revoked(false)
                .build();
        outroToken = tokenRepository.saveAndFlush(outroToken);

        tokenRepository.delete(outroToken);
        tokenRepository.flush();
        usuarioRepository.delete(usuarioFalso);
        usuarioRepository.flush();

        var request = new AtivacaoRequest(
                outroToken.getToken(),
                "novaSenha",
                "(11)90000-0000",
                new EnderecoRequest("00000-000", "Rua", "123", null, "Bairro", "Cidade", "SP")
        );

        mockMvc.perform(post("/ativar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token inválido ou malformado."));
    }

    @Test
    void deveNegarAtivacao_QuandoTokenMalformado() throws Exception {
        var request = new AtivacaoRequest(
                "token-invalido",
                "NovaSenha123",
                "(11)99999-0000",
                new EnderecoRequest("12345-678", "Rua A", "10", null, "Centro", "São Paulo", "SP")
        );

        mockMvc.perform(post("/ativar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token inválido ou malformado."));
    }
}