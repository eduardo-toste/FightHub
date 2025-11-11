package com.fighthub.integration;

import com.fighthub.dto.turma.TurmaRequest;
import com.fighthub.dto.turma.TurmaUpdateCompletoRequest;
import com.fighthub.dto.turma.TurmaUpdateStatusRequest;
import com.fighthub.model.*;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TurmaIntegrationTest extends IntegrationTestBase {

    @SpyBean private TokenService tokenService;
    @Autowired private JwtService jwtService;

    private Usuario usuario;
    private Professor professor;
    private Turma turma;
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

        usuario = usuarioRepository.save(
                Usuario.builder()
                        .nome("Usuário Teste")
                        .email("usuario@email.com")
                        .cpf("935.449.680-61")
                        .telefone("(11)98888-0000")
                        .role(Role.ADMIN)
                        .ativo(true)
                        .loginSocial(false)
                        .endereco(endereco)
                        .build()
        );

        accessToken = jwtService.gerarToken(usuario);
        tokenService.salvarAccessToken(usuario, accessToken);

        professor = professorRepository.save(Professor.builder().usuario(usuario).build());

        turma = turmaRepository.save(Turma.builder()
                .nome("Turma de Segunda")
                .horario("Segunda 19:00")
                .professor(professor)
                .ativo(true)
                .alunos(new ArrayList<>())
                .build());
    }

    @Test
    void deveCriarTurmaComSucesso() throws Exception {
        var request = new TurmaRequest("Turma de Domingo", "Domingo 10:00", professor.getId());

        mockMvc.perform(post("/turmas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertEquals(2, turmaRepository.count());
    }

    @Test
    void deveRetornar404_AoCriarTurma_QuandoProfessorNaoExistir() throws Exception {
        var request = new TurmaRequest("Turma Inválida", "Domingo 10:00", UUID.randomUUID());

        mockMvc.perform(post("/turmas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        assertEquals(1, turmaRepository.count());
    }

    @Test
    void deveRetornar400_QuandoDadosInvalidosNaCriacao() throws Exception {
        var request = new TurmaRequest("", "", null);

        mockMvc.perform(post("/turmas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveListarTurmasComSucesso() throws Exception {
        mockMvc.perform(get("/turmas")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome", is(turma.getNome())))
                .andExpect(jsonPath("$.content[0].horario", is(turma.getHorario())))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.size", greaterThan(0)));
    }

    @Test
    void deveBuscarTurmaPorIdComSucesso() throws Exception {
        mockMvc.perform(get("/turmas/{id}", turma.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(turma.getId().toString())))
                .andExpect(jsonPath("$.nome", is(turma.getNome())))
                .andExpect(jsonPath("$.horario", is(turma.getHorario())))
                .andExpect(jsonPath("$.ativo", is(true)));
    }

    @Test
    void deveRetornar404_AoBuscarTurmaInexistente() throws Exception {
        mockMvc.perform(get("/turmas/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarTurmaComSucesso() throws Exception {
        var request = new TurmaUpdateCompletoRequest("Turma Atualizada", "Terça 19:00", professor.getId(), false);

        mockMvc.perform(put("/turmas/{id}", turma.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Turma Atualizada")))
                .andExpect(jsonPath("$.horario", is("Terça 19:00")))
                .andExpect(jsonPath("$.ativo", is(false)));

        var turmaAtualizada = turmaRepository.findById(turma.getId()).orElseThrow();
        assertEquals("Turma Atualizada", turmaAtualizada.getNome());
        assertFalse(turmaAtualizada.isAtivo());
    }

    @Test
    void deveRetornar404_AoAtualizarTurmaInexistente() throws Exception {
        var request = new TurmaUpdateCompletoRequest("Inexistente", "00:00", professor.getId(), true);

        mockMvc.perform(put("/turmas/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarStatusTurmaComSucesso() throws Exception {
        var request = new TurmaUpdateStatusRequest(false);

        mockMvc.perform(patch("/turmas/{id}/status", turma.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo", is(false)));

        assertFalse(turmaRepository.findById(turma.getId()).orElseThrow().isAtivo());
    }

    @Test
    void deveRetornar404_AoAtualizarStatusDeTurmaInexistente() throws Exception {
        var request = new TurmaUpdateStatusRequest(false);

        mockMvc.perform(patch("/turmas/{id}/status", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveExcluirTurmaComSucesso() throws Exception {
        mockMvc.perform(delete("/turmas/{id}", turma.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        assertFalse(turmaRepository.findById(turma.getId()).isPresent());
    }

    @Test
    void deveRetornar404_AoExcluirTurmaInexistente() throws Exception {
        mockMvc.perform(delete("/turmas/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveVincularProfessorComSucesso() throws Exception {
        Professor novoProfessor = professorRepository.save(
                Professor.builder().usuario(usuarioRepository.save(
                        Usuario.builder()
                                .nome("Outro Professor")
                                .email("outro@email.com")
                                .cpf("563.184.170-40")
                                .telefone("(11)97777-1111")
                                .role(Role.PROFESSOR)
                                .ativo(true)
                                .loginSocial(false)
                                .endereco(usuario.getEndereco())
                                .build()
                )).build()
        );

        mockMvc.perform(patch("/turmas/{idTurma}/professores/{idProfessor}", turma.getId(), novoProfessor.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        var turmaAtualizada = turmaRepository.findById(turma.getId()).orElseThrow();
        assertEquals(novoProfessor.getId(), turmaAtualizada.getProfessor().getId());
    }

    @Test
    void deveRetornar404_AoVincularProfessorInexistente() throws Exception {
        mockMvc.perform(patch("/turmas/{idTurma}/professores/{idProfessor}", turma.getId(), UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveDesvincularProfessorComSucesso() throws Exception {
        mockMvc.perform(delete("/turmas/{idTurma}/professores/{idProfessor}", turma.getId(), professor.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        var turmaAtualizada = turmaRepository.findById(turma.getId()).orElseThrow();
        assertNull(turmaAtualizada.getProfessor());
    }

    @Test
    void deveRetornar404_AoDesvincularProfessorInexistente() throws Exception {
        mockMvc.perform(delete("/turmas/{idTurma}/professores/{idProfessor}", turma.getId(), UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveVincularAlunoComSucesso() throws Exception {
        var aluno = alunoRepository.save(buildAlunoValido(usuario.getEndereco()));

        mockMvc.perform(patch("/turmas/{idTurma}/alunos/{idAluno}", turma.getId(), aluno.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        var turmaAtualizada = turmaRepository.findById(turma.getId()).orElseThrow();
        assertTrue(turmaAtualizada.getAlunos().contains(aluno));
    }

    @Test
    void deveRetornar404_AoVincularAlunoInexistente() throws Exception {
        mockMvc.perform(patch("/turmas/{idTurma}/alunos/{idAluno}", turma.getId(), UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveDesvincularAlunoComSucesso() throws Exception {
        var aluno = alunoRepository.save(buildAlunoValido(usuario.getEndereco()));
        turma.getAlunos().add(aluno);
        turmaRepository.saveAndFlush(turma);

        mockMvc.perform(delete("/turmas/{idTurma}/alunos/{idAluno}", turma.getId(), aluno.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        var turmaAtualizada = turmaRepository.findById(turma.getId()).orElseThrow();
        assertFalse(turmaAtualizada.getAlunos().contains(aluno));
    }

    @Test
    void deveRetornar404_AoDesvincularAlunoInexistente() throws Exception {
        mockMvc.perform(delete("/turmas/{idTurma}/alunos/{idAluno}", turma.getId(), UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    private Aluno buildAlunoValido(Endereco endereco) {
        Usuario user = usuarioRepository.save(
                Usuario.builder()
                        .nome("Aluno Teste")
                        .email("aluno@email.com")
                        .cpf("329.235.300-85")
                        .telefone("(11)99999-1234")
                        .role(Role.ALUNO)
                        .ativo(true)
                        .loginSocial(false)
                        .endereco(endereco)
                        .build()
        );

        return alunoRepository.save(Aluno.builder()
                .usuario(user)
                .dataNascimento(LocalDate.of(2000, 1, 1)) // ou LocalDate.now().minusYears(18)
                .dataMatricula(LocalDate.now())
                .matriculaAtiva(true)
                .build()
        );
    }
}