package com.fighthub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fighthub.config.TestSecurityConfig;
import com.fighthub.dto.aluno.AlunoResponse;
import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.dto.endereco.EnderecoResponse;
import com.fighthub.dto.usuario.*;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.TokenRepository;
import com.fighthub.repository.UsuarioRepository;
import com.fighthub.service.AlunoService;
import com.fighthub.service.AuthService;
import com.fighthub.service.JwtService;
import com.fighthub.service.UsuarioService;
import com.fighthub.utils.ErrorWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UsuarioController.class)
@Import(TestSecurityConfig.class)
class UsuarioControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UsuarioRepository usuarioRepository;
    @MockBean private TokenRepository tokenRepository;
    @MockBean private UsuarioService usuarioService;
    @MockBean private AuthService authService;
    @MockBean private JwtService jwtService;
    @MockBean private ErrorWriter errorWriter;

    private static final String TOKEN = "token-valido";

    @BeforeEach
    void setupSecurity() {
        when(jwtService.tokenValido(anyString())).thenReturn(true);
        when(jwtService.extrairEmail(anyString())).thenReturn("usuario@teste.com");

        var usuarioMock = Usuario.builder()
                .id(UUID.randomUUID())
                .email("usuario@teste.com")
                .nome("Usuário Teste")
                .ativo(true)
                .role(Role.ADMIN)
                .build();

        when(usuarioRepository.findByEmail("usuario@teste.com"))
                .thenReturn(Optional.of(usuarioMock));

        when(tokenRepository.findByTokenAndExpiredFalseAndRevokedFalse(anyString()))
                .thenReturn(Optional.of(new com.fighthub.model.Token()));
    }

    @Test
    void deveRetornarPaginaDeUsuarios() throws Exception {
        Page<UsuarioResponse> page = new PageImpl<>(List.of(
                new UsuarioResponse(
                        UUID.randomUUID(),
                        "João",
                        "111.111.111-44",
                        "joao@email.com",
                        "(11)99999-9999",
                        Role.ALUNO,
                        true
                )
        ));
        when(usuarioService.obterTodosUsuarios(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("João"))
                .andExpect(jsonPath("$.content[0].email").value("joao@email.com"))
                .andExpect(jsonPath("$.content[0].cpf").value("111.111.111-44"))
                .andExpect(jsonPath("$.content[0].ativo").value(true));

        verify(usuarioService).obterTodosUsuarios(any(Pageable.class));
    }

    @Test
    void deveRetornarUsuarioPorId() throws Exception {
        var userId = UUID.randomUUID();
        var response = new UsuarioDetalhadoResponse(
                userId,
                "João",
                "111.111.111-44",
                "joao@email.com",
                "(11)99999-9999",
                null,
                Role.ALUNO,
                false,
                true,
                new EnderecoResponse("12345-678", "Rua das Flores", "123", "Apto 45", "Centro", "São Paulo", "SP")
        );
        when(usuarioService.obterUsuario(userId)).thenReturn(response);

        mockMvc.perform(get("/usuarios/{id}", userId)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.nome").value("João"));

        verify(usuarioService).obterUsuario(userId);
    }

    @Test
    void deveAtualizarRoleUsuario() throws Exception {
        var userId = UUID.randomUUID();
        var request = new UpdateRoleRequest(Role.ALUNO);
        var response = new UsuarioResponse(
                UUID.randomUUID(),
                "João",
                "111.111.111-44",
                "joao@email.com",
                "(11)99999-9999",
                Role.ALUNO,
                true
        );
        when(usuarioService.updateRole(userId, request)).thenReturn(response);

        mockMvc.perform(patch("/usuarios/{id}/role", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + TOKEN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.role").value("ALUNO"));

        verify(usuarioService).updateRole(userId, request);
    }

    @Test
    void deveAtualizarStatusUsuario() throws Exception {
        var userId = UUID.randomUUID();
        var request = new UpdateStatusRequest(true);
        var response = new UsuarioResponse(
                UUID.randomUUID(),
                "João",
                "111.111.111-44",
                "joao@email.com",
                "(11)99999-9999",
                Role.ALUNO,
                true
        );
        when(usuarioService.updateStatus(userId, request)).thenReturn(response);

        mockMvc.perform(patch("/usuarios/{id}/status", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + TOKEN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.ativo").value(true));

        verify(usuarioService).updateStatus(userId, request);
    }

    @Test
    void deveAtualizarUsuarioPorCompleto() throws Exception {
        var userId = UUID.randomUUID();
        var enderecoRequest = new EnderecoRequest(
                "12345-677",
                "Rua da Flor",
                "113",
                "Apto 44",
                "Centro",
                "São Paulo",
                "SP"
        );
        var request = new UsuarioUpdateCompletoRequest(
                "Nome Atualizado",
                "email_att@example.com",
                null,
                "(11)12346-5897",
                "917.570.460-97",
                enderecoRequest,
                Role.ALUNO,
                true
        );
        var response = new UsuarioDetalhadoResponse(
                userId,
                "Nome Atualizado",
                "917.570.460-97",
                "email_att@example.com",
                "(11)12346-5897",
                null,
                Role.ALUNO,
                false,
                true,
                new EnderecoResponse("12345-677",
                        "Rua da Flor",
                        "113",
                        "Apto 44",
                        "Centro",
                        "São Paulo",
                        "SP")
        );
        when(usuarioService.updateUsuarioCompleto(userId, request)).thenReturn(response);

        mockMvc.perform(put("/usuarios/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + TOKEN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"))
                .andExpect(jsonPath("$.cpf").value("917.570.460-97"))
                .andExpect(jsonPath("$.email").value("email_att@example.com"))
                .andExpect(jsonPath("$.telefone").value("(11)12346-5897"))
                .andExpect(jsonPath("$.foto").value(nullValue()))
                .andExpect(jsonPath("$.role").value("ALUNO"))
                .andExpect(jsonPath("$.loginSocial").value(false))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.endereco.cep").value("12345-677"))
                .andExpect(jsonPath("$.endereco.logradouro").value("Rua da Flor"))
                .andExpect(jsonPath("$.endereco.numero").value("113"))
                .andExpect(jsonPath("$.endereco.complemento").value("Apto 44"))
                .andExpect(jsonPath("$.endereco.bairro").value("Centro"))
                .andExpect(jsonPath("$.endereco.cidade").value("São Paulo"))
                .andExpect(jsonPath("$.endereco.estado").value("SP"));

        verify(usuarioService).updateUsuarioCompleto(userId, request);
    }

    @Test
    void deveAtualizarUsuarioParcialmente() throws Exception {
        var userId = UUID.randomUUID();
        var request = new UsuarioUpdateParcialRequest(
                "Nome Atualizado",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        var response = new UsuarioDetalhadoResponse(
                userId,
                "Nome Atualizado",
                "917.570.460-97",
                "email_att@example.com",
                "(11)12346-5897",
                null,
                Role.ALUNO,
                false,
                true,
                new EnderecoResponse("12345-677",
                        "Rua da Flor",
                        "113",
                        "Apto 44",
                        "Centro",
                        "São Paulo",
                        "SP")
        );
        when(usuarioService.updateUsuarioParcial(userId, request)).thenReturn(response);

        mockMvc.perform(patch("/usuarios/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + TOKEN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"));

        verify(usuarioService).updateUsuarioParcial(userId, request);
    }

    @Test
    void deveRetornarNotFoundAoBuscarUsuarioInexistente() throws Exception {
        UUID id = UUID.randomUUID();
        when(usuarioService.obterUsuario(id))
                .thenThrow(new UsuarioNaoEncontradoException());

        mockMvc.perform(get("/usuarios/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundAoAtualizarRoleUsuarioInexistente() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateRoleRequest request = new UpdateRoleRequest(Role.ALUNO);

        when(usuarioService.updateRole(eq(id), any(UpdateRoleRequest.class)))
                .thenThrow(new UsuarioNaoEncontradoException());

        mockMvc.perform(patch("/usuarios/{id}/role", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + TOKEN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarConflictAoAtualizarStatusJaDefinido() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateStatusRequest request = new UpdateStatusRequest(true);

        when(usuarioService.updateStatus(eq(id), any(UpdateStatusRequest.class)))
                .thenThrow(new ValidacaoException("Usuário já está ativo"));

        mockMvc.perform(patch("/usuarios/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + TOKEN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornarErroInternoAoAtualizarUsuarioCompleto() throws Exception {
        UUID id = UUID.randomUUID();
        UsuarioUpdateCompletoRequest request = new UsuarioUpdateCompletoRequest(
                "Nome",
                "email@example.com",
                null,
                "(11)99999-9999",
                "917.570.460-97",
                new EnderecoRequest("12345-000", "Rua Teste", "100", null, "Centro", "SP", "SP"),
                Role.ALUNO,
                true
        );

        when(usuarioService.updateUsuarioCompleto(eq(id), any(UsuarioUpdateCompletoRequest.class)))
                .thenThrow(new RuntimeException("Erro inesperado"));

        mockMvc.perform(put("/usuarios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + TOKEN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deveRetornarBadRequestQuandoCamposInvalidosNoUpdateCompleto() throws Exception {
        UUID id = UUID.randomUUID();
        UsuarioUpdateCompletoRequest request = new UsuarioUpdateCompletoRequest(
                "",
                "email_invalido",
                null,
                "123",
                "cpf_invalido",
                new EnderecoRequest("123", "", "", null, "", "", ""), // endereço inválido
                null,
                true
        );

        mockMvc.perform(put("/usuarios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + TOKEN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarBadRequestQuandoRoleForNula() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateRoleRequest request = new UpdateRoleRequest(null);

        mockMvc.perform(patch("/usuarios/{id}/role", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + TOKEN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarBadRequestQuandoStatusForNulo() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateStatusRequest request = new UpdateStatusRequest(null);

        mockMvc.perform(patch("/usuarios/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + TOKEN)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}