package com.fighthub.integration;

import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.dto.usuario.*;
import com.fighthub.model.Endereco;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.service.JwtService;
import com.fighthub.service.TokenService;
import com.fighthub.utils.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UsuarioIntegrationTest extends IntegrationTestBase {

    @Autowired private JwtService jwtService;
    @SpyBean private TokenService tokenService;

    private Usuario admin;
    private Usuario aluno;
    private String tokenAdmin;
    private String tokenAluno;

    @BeforeEach
    void setup() {
        Endereco endereco = Endereco.builder()
                .cep("01000-000")
                .logradouro("Rua Teste")
                .numero("123")
                .bairro("Centro")
                .cidade("São Paulo")
                .estado("SP")
                .build();

        admin = usuarioRepository.save(Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Admin User")
                .email("admin@email.com")
                .cpf("111.111.111-11")
                .telefone("(11)99999-0000")
                .role(Role.ADMIN)
                .ativo(true)
                .senha("123456")
                .endereco(endereco)
                .build());

        aluno = usuarioRepository.save(Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Aluno User")
                .email("aluno@email.com")
                .cpf("222.222.222-22")
                .telefone("(11)98888-0000")
                .role(Role.ALUNO)
                .ativo(true)
                .senha("123456")
                .endereco(endereco)
                .build());

        tokenAdmin = jwtService.gerarToken(admin);
        tokenAluno = jwtService.gerarToken(aluno);

        tokenService.salvarAccessToken(admin, tokenAdmin);
        tokenService.salvarAccessToken(aluno, tokenAluno);
    }

    // --------------------- GET /usuarios ---------------------

    @Test
    void deveRetornarPageDeUsuariosComSucesso() throws Exception {
        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.content[0].email", notNullValue()))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(2)));
    }

    @Test
    void deveRetornar403_AoListarUsuarios_QuandoNaoForAdmin() throws Exception {
        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + tokenAluno))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar401_AoListarUsuarios_SemToken() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    // --------------------- GET /usuarios/{id} ---------------------

    @Test
    void deveRetornarUsuarioPorIdComSucesso() throws Exception {
        mockMvc.perform(get("/usuarios/{id}", aluno.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(aluno.getId().toString()))
                .andExpect(jsonPath("$.nome").value("Aluno User"))
                .andExpect(jsonPath("$.email").value("aluno@email.com"));
    }

    @Test
    void deveRetornar404_AoBuscarUsuarioPorIdInexistente() throws Exception {
        mockMvc.perform(get("/usuarios/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar403_AoBuscarUsuarioPorId_QuandoNaoForAdmin() throws Exception {
        mockMvc.perform(get("/usuarios/{id}", admin.getId())
                        .header("Authorization", "Bearer " + tokenAluno))
                .andExpect(status().isForbidden());
    }

    // --------------------- PATCH /usuarios/{id}/role ---------------------

    @Test
    void deveAtualizarRoleDoUsuarioComSucesso() throws Exception {
        var request = new UpdateRoleRequest(Role.PROFESSOR);

        mockMvc.perform(patch("/usuarios/{id}/role", aluno.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PROFESSOR"));

        var usuarioAtualizado = usuarioRepository.findById(aluno.getId()).get();
        assertEquals(Role.PROFESSOR, usuarioAtualizado.getRole());
    }

    @Test
    void deveRetornar409_AoAtualizarRoleComMesmoValor() throws Exception {
        var request = new UpdateRoleRequest(Role.ALUNO);

        mockMvc.perform(patch("/usuarios/{id}/role", aluno.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar404_AoAtualizarRoleDeUsuarioInexistente() throws Exception {
        var request = new UpdateRoleRequest(Role.PROFESSOR);

        mockMvc.perform(patch("/usuarios/{id}/role", UUID.randomUUID())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar403_AoAtualizarRole_QuandoNaoForAdmin() throws Exception {
        var request = new UpdateRoleRequest(Role.PROFESSOR);

        mockMvc.perform(patch("/usuarios/{id}/role", aluno.getId())
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // --------------------- PATCH /usuarios/{id}/status ---------------------

    @Test
    void deveAtualizarStatusDoUsuarioComSucesso() throws Exception {
        var request = new UpdateStatusRequest(false);

        mockMvc.perform(patch("/usuarios/{id}/status", aluno.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));

        var usuarioAtualizado = usuarioRepository.findById(aluno.getId()).get();
        assertFalse(usuarioAtualizado.isAtivo());
    }

    @Test
    void deveRetornar409_AoAtualizarStatusComMesmoValor() throws Exception {
        var request = new UpdateStatusRequest(true);

        mockMvc.perform(patch("/usuarios/{id}/status", aluno.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar404_AoAtualizarStatusDeUsuarioInexistente() throws Exception {
        var request = new UpdateStatusRequest(false);

        mockMvc.perform(patch("/usuarios/{id}/status", UUID.randomUUID())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // --------------------- PUT /usuarios/{id} ---------------------

    @Test
    void deveAtualizarUsuarioCompletoComSucesso() throws Exception {
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
                "Aluno Atualizado",
                "email_att@example.com",
                null,
                "(11)12346-5897",
                "583.804.360-16",
                enderecoRequest,
                Role.ALUNO,
                true
        );

        mockMvc.perform(put("/usuarios/{id}", aluno.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Aluno Atualizado"))
                .andExpect(jsonPath("$.email").value("email_att@example.com"));

        var usuarioAtualizado = usuarioRepository.findById(aluno.getId()).get();
        assertEquals("Aluno Atualizado", usuarioAtualizado.getNome());
        assertEquals("email_att@example.com", usuarioAtualizado.getEmail());
    }

    @Test
    void deveRetornar404_AoAtualizarUsuarioCompletoInexistente() throws Exception {
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
                "583.804.360-16",
                enderecoRequest,
                Role.ALUNO,
                true
        );

        mockMvc.perform(put("/usuarios/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // --------------------- PATCH /usuarios/{id} ---------------------

    @Test
    void deveAtualizarUsuarioParcialComSucesso() throws Exception {
        var request = new UsuarioUpdateParcialRequest("Nome Parcial", null, null, null, null, null, null, null);

        mockMvc.perform(patch("/usuarios/{id}", aluno.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Parcial"));
    }

    @Test
    void deveRetornar404_AoAtualizarUsuarioParcialInexistente() throws Exception {
        var request = new UsuarioUpdateParcialRequest("Novo Nome", null, null, null, null, null, null, null);

        mockMvc.perform(patch("/usuarios/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // --------------------- GET /usuarios/me ---------------------

    @Test
    void deveRetornarDadosPropriosComSucesso() throws Exception {
        mockMvc.perform(get("/usuarios/me")
                        .header("Authorization", "Bearer " + tokenAluno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("aluno@email.com"))
                .andExpect(jsonPath("$.nome").value("Aluno User"));
    }

    @Test
    void deveRetornar401_AoBuscarPropriosDados_SemToken() throws Exception {
        mockMvc.perform(get("/usuarios/me"))
                .andExpect(status().isUnauthorized());
    }

    // --------------------- PUT /usuarios/me ---------------------

    @Test
    void deveAtualizarProprioUsuarioComSucesso() throws Exception {
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
                "583.804.360-16",
                enderecoRequest,
                Role.ALUNO,
                true
        );

        mockMvc.perform(put("/usuarios/me")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"))
                .andExpect(jsonPath("$.email").value("email_att@example.com"));
    }

    // --------------------- PATCH /usuarios/me ---------------------

    @Test
    void deveAtualizarProprioUsuarioParcialmente() throws Exception {
        var request = new UsuarioUpdateParcialRequest("Nome Novo", null, null, null, null, null, null, null);

        mockMvc.perform(patch("/usuarios/me")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Novo"));
    }

    // --------------------- PATCH /usuarios/me/password ---------------------

    @Test
    void deveAtualizarSenhaComSucesso() throws Exception {
        var request = new UpdateSenhaRequest("novaSenha123");

        mockMvc.perform(patch("/usuarios/me/password")
                        .header("Authorization", "Bearer " + tokenAluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        var usuario = usuarioRepository.findById(aluno.getId()).get();
        assertNotEquals("123456", usuario.getSenha());
    }

    @Test
    void deveRetornar401_AoAtualizarSenha_SemToken() throws Exception {
        var request = new UpdateSenhaRequest("senha123");

        mockMvc.perform(patch("/usuarios/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}