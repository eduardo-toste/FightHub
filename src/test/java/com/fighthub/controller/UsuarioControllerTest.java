package com.fighthub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.dto.endereco.EnderecoResponse;
import com.fighthub.dto.usuario.*;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.model.enums.Role;
import com.fighthub.service.UsuarioService;
import com.fighthub.utils.ControllerTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageImpl;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UsuarioController.class)
class UsuarioControllerTest extends ControllerTestBase {

    @MockBean
    private UsuarioService usuarioService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarPaginaDeUsuarios() throws Exception {
        var usuarios = List.of(new UsuarioResponse(
                UUID.randomUUID(),
                "João",
                "111.111.111-44",
                "joao@email.com",
                "(11)99999-9999",
                Role.ALUNO,
                true
        ));

        var page = new PageImpl<>(usuarios);
        when(usuarioService.obterTodosUsuarios(any())).thenReturn(page);

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("João"))
                .andExpect(jsonPath("$.content[0].email").value("joao@email.com"))
                .andExpect(jsonPath("$.content[0].cpf").value("111.111.111-44"))
                .andExpect(jsonPath("$.content[0].ativo").value(true));

        verify(usuarioService).obterTodosUsuarios(any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarUsuarioPorId() throws Exception {
        var id = UUID.randomUUID();
        var response = new UsuarioDetalhadoResponse(
                id,
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

        when(usuarioService.obterUsuario(id)).thenReturn(response);

        mockMvc.perform(get("/usuarios/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("João"));

        verify(usuarioService).obterUsuario(id);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveAtualizarRoleUsuario() throws Exception {
        var id = UUID.randomUUID();
        var request = new UpdateRoleRequest(Role.ALUNO);
        var response = new UsuarioResponse(
                id,
                "João",
                "111.111.111-44",
                "joao@email.com",
                "(11)99999-9999",
                Role.ALUNO,
                true
        );

        when(usuarioService.updateRole(id, request)).thenReturn(response);

        mockMvc.perform(patch("/usuarios/{id}/role", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ALUNO"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));

        verify(usuarioService).updateRole(id, request);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveAtualizarStatusUsuario() throws Exception {
        var id = UUID.randomUUID();
        var request = new UpdateStatusRequest(true);
        var response = new UsuarioResponse(
                id,
                "João",
                "111.111.111-44",
                "joao@email.com",
                "(11)99999-9999",
                Role.ALUNO,
                true
        );

        when(usuarioService.updateStatus(id, request)).thenReturn(response);

        mockMvc.perform(patch("/usuarios/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));

        verify(usuarioService).updateStatus(id, request);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveAtualizarUsuarioPorCompleto() throws Exception {
        var id = UUID.randomUUID();
        var endereco = new EnderecoRequest("12345-677", "Rua da Flor", "113", "Apto 44", "Centro", "São Paulo", "SP");
        var request = new UsuarioUpdateCompletoRequest("Nome Atualizado", "email_att@example.com", null,
                "(11)12346-5897", "917.570.460-97", endereco, Role.ALUNO, true);

        var response = new UsuarioDetalhadoResponse(
                id,
                "Nome Atualizado",
                "917.570.460-97",
                "email_att@example.com",
                "(11)12346-5897",
                null,
                Role.ALUNO,
                false,
                true,
                new EnderecoResponse("12345-677", "Rua da Flor", "113", "Apto 44", "Centro", "São Paulo", "SP")
        );

        when(usuarioService.updateUsuarioCompleto(id, request)).thenReturn(response);

        mockMvc.perform(put("/usuarios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"))
                .andExpect(jsonPath("$.cpf").value("917.570.460-97"))
                .andExpect(jsonPath("$.email").value("email_att@example.com"));

        verify(usuarioService).updateUsuarioCompleto(id, request);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveAtualizarUsuarioParcialmente() throws Exception {
        var id = UUID.randomUUID();
        var request = new UsuarioUpdateParcialRequest("Nome Atualizado", null, null, null, null, null, null, null);
        var response = new UsuarioDetalhadoResponse(
                id,
                "Nome Atualizado",
                "917.570.460-97",
                "email_att@example.com",
                "(11)12346-5897",
                null,
                Role.ALUNO,
                false,
                true,
                null
        );

        when(usuarioService.updateUsuarioParcial(id, request)).thenReturn(response);

        mockMvc.perform(patch("/usuarios/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"));

        verify(usuarioService).updateUsuarioParcial(id, request);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarNotFound_QuandoUsuarioNaoExistir() throws Exception {
        UUID id = UUID.randomUUID();
        when(usuarioService.obterUsuario(id)).thenThrow(new UsuarioNaoEncontradoException());

        mockMvc.perform(get("/usuarios/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado."));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarConflict_QuandoStatusJaDefinido() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new UpdateStatusRequest(true);

        doThrow(new ValidacaoException("Usuário já está ativo"))
                .when(usuarioService).updateStatus(id, request);

        mockMvc.perform(patch("/usuarios/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Usuário já está ativo"));

        verify(usuarioService).updateStatus(id, request);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarBadRequest_QuandoRoleForNula() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new UpdateRoleRequest(null);

        mockMvc.perform(patch("/usuarios/{id}/role", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveAtualizarSenhaDoUsuario() throws Exception {
        var requestDTO = new UpdateSenhaRequest("novaSenhaSegura123");

        mockMvc.perform(patch("/usuarios/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());

        verify(usuarioService).updateSenha(any(), eq(requestDTO));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarBadRequest_QuandoSenhaForInvalida() throws Exception {
        var requestDTO = new UpdateSenhaRequest("");

        mockMvc.perform(patch("/usuarios/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }
}