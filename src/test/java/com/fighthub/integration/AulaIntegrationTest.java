package com.fighthub.integration;

import com.fighthub.dto.aula.AulaRequest;
import com.fighthub.dto.aula.AulaUpdateCompletoRequest;
import com.fighthub.dto.aula.AulaUpdateStatusRequest;
import com.fighthub.model.*;
import com.fighthub.model.enums.ClassStatus;
import com.fighthub.model.enums.Role;
import com.fighthub.service.JwtService;
import com.fighthub.service.TokenService;
import com.fighthub.utils.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AulaIntegrationTest extends IntegrationTestBase {

    @SpyBean private TokenService tokenService;
    @Autowired private JwtService jwtService;

    private Usuario usuario;
    private Turma turma;
    private Aula aula;
    private String accessToken;

    @BeforeEach
    void setup() {
        Endereco endereco = Endereco.builder()
                .cep("12345-678")
                .logradouro("Rua das Flores")
                .numero("123")
                .bairro("Centro")
                .cidade("São Paulo")
                .estado("SP")
                .build();

        usuario = usuarioRepository.save(Usuario.builder()
                .nome("Usuário Teste")
                .email("usuario@email.com")
                .cpf("935.449.680-61")
                .telefone("(11)98888-0000")
                .role(Role.ADMIN)
                .ativo(true)
                .loginSocial(false)
                .endereco(endereco)
                .build());

        accessToken = jwtService.gerarToken(usuario);
        tokenService.salvarAccessToken(usuario, accessToken);

        turma = turmaRepository.save(Turma.builder()
                .nome("Turma de Teste")
                .horario("Segunda 19:00")
                .ativo(true)
                .alunos(new ArrayList<>())
                .build());

        aula = aulaRepository.save(Aula.builder()
                .titulo("Aula Inicial")
                .descricao("Descrição da Aula")
                .data(LocalDate.now().plusDays(1))
                .status(ClassStatus.DISPONIVEL)
                .limiteAlunos(20)
                .ativo(true)
                .turma(turma)
                .build());
    }

    @Test
    void deveCriarAulaComSucesso() throws Exception {
        AulaRequest request = new AulaRequest("Nova Aula", "Conteúdo", LocalDate.now().plusDays(5), turma.getId(), 15);

        mockMvc.perform(post("/aulas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void deveCriarAulaSemTurma() throws Exception {
        AulaRequest request = new AulaRequest("Aula Solta", "Sem turma", LocalDate.now().plusDays(3), null, 10);

        mockMvc.perform(post("/aulas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void deveListarAulasComSucesso() throws Exception {
        mockMvc.perform(get("/aulas")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].titulo", is(aula.getTitulo())));
    }

    @Test
    void deveBuscarAulaPorIdComSucesso() throws Exception {
        mockMvc.perform(get("/aulas/{id}", aula.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is(aula.getTitulo())));
    }

    @Test
    void deveRetornar404_QuandoBuscarAulaInexistente() throws Exception {
        mockMvc.perform(get("/aulas/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarAulaComSucesso() throws Exception {
        AulaUpdateCompletoRequest request = new AulaUpdateCompletoRequest(
                "Aula Atualizada", "Nova descrição", LocalDate.now().plusDays(2), turma.getId(), 30, true);

        mockMvc.perform(put("/aulas/{id}", aula.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is("Aula Atualizada")))
                .andExpect(jsonPath("$.descricao", is("Nova descrição")));
    }

    @Test
    void deveAtualizarStatusAulaComSucesso() throws Exception {
        AulaUpdateStatusRequest request = new AulaUpdateStatusRequest(ClassStatus.CANCELADA);

        mockMvc.perform(patch("/aulas/{id}/status", aula.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELADA")));
    }

    @Test
    void deveExcluirAulaComSucesso() throws Exception {
        mockMvc.perform(delete("/aulas/{id}", aula.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        assertFalse(aulaRepository.findById(aula.getId()).isPresent());
    }

    @Test
    void deveRetornar404_AoExcluirAulaInexistente() throws Exception {
        mockMvc.perform(delete("/aulas/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveVincularTurmaComSucesso() throws Exception {
        Turma novaTurma = turmaRepository.save(Turma.builder()
                .nome("Turma X")
                .horario("Quarta 18:00")
                .ativo(true)
                .build());

        mockMvc.perform(patch("/aulas/{idAula}/turmas/{idTurma}", aula.getId(), novaTurma.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void deveDesvincularTurmaComSucesso() throws Exception {
        mockMvc.perform(delete("/aulas/{idAula}/turmas/{idTurma}", aula.getId(), turma.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        var atualizada = aulaRepository.findById(aula.getId()).orElseThrow();
        assertNull(atualizada.getTurma());
    }

    @Test
    void deveRetornar404_AoVincularTurmaInexistente() throws Exception {
        mockMvc.perform(patch("/aulas/{idAula}/turmas/{idTurma}", aula.getId(), UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404_AoDesvincularTurmaInexistente() throws Exception {
        mockMvc.perform(delete("/aulas/{idAula}/turmas/{idTurma}", aula.getId(), UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
}