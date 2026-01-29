package com.fighthub.controller;

import com.fighthub.dto.aluno.*;
import com.fighthub.dto.endereco.EnderecoResponse;
import com.fighthub.exception.AlunoNaoEncontradoException;
import com.fighthub.exception.MatriculaInvalidaException;
import com.fighthub.service.AlunoService;
import com.fighthub.utils.ControllerTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AlunoController.class)
class AlunoControllerTest extends ControllerTestBase {

    @MockBean private AlunoService alunoService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveCriarAluno() throws Exception {
        CriarAlunoRequest request = new CriarAlunoRequest(
                "João",
                "joao@email.com",
                "390.533.447-05",
                LocalDate.now().minusYears(17),
                List.of(UUID.randomUUID())
        );

        doNothing().when(alunoService).criarAluno(any(CriarAlunoRequest.class));

        mockMvc.perform(post("/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(alunoService).criarAluno(any(CriarAlunoRequest.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarPaginaDeAlunos() throws Exception {
        var page = new org.springframework.data.domain.PageImpl<>(List.of(
                new AlunoResponse(
                        UUID.randomUUID(),
                        "João",
                        "joao@email.com",
                        "(11)99999-9999",
                        null, // foto
                        LocalDate.of(2003, 10, 15),
                        LocalDate.now(),
                        true,
                        null, // graduacao
                        List.of() // turmaIds
                )
        ));

        when(alunoService.obterTodos(any())).thenReturn(page);

        mockMvc.perform(get("/alunos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("João"));

        verify(alunoService).obterTodos(any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarAlunoPorId() throws Exception {
        UUID id = UUID.randomUUID();
        AlunoDetalhadoResponse response = new AlunoDetalhadoResponse(
                id,
                "João",
                "joao@email.com",
                "(11)99999-9999",
                null, // foto
                LocalDate.of(2003, 10, 15),
                LocalDate.now(),
                true,
                null, // graduacaoAluno
                new EnderecoResponse("12345-678", "Rua das Flores", "123", "Apto 45", "Centro", "São Paulo", "SP"),
                List.of() // responsaveis
        );

        when(alunoService.obterAluno(id)).thenReturn(response);

        mockMvc.perform(get("/alunos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(alunoService).obterAluno(id);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveAtualizarStatusMatricula_QuandoSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new AlunoUpdateMatriculaRequest(true);

        doNothing().when(alunoService).atualizarStatusMatricula(id, request);

        mockMvc.perform(patch("/alunos/{id}/matricula", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(alunoService).atualizarStatusMatricula(id, request);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarNotFound_QuandoAlunoNaoExistir_AoAtualizarMatricula() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new AlunoUpdateMatriculaRequest(false);

        doThrow(new AlunoNaoEncontradoException())
                .when(alunoService).atualizarStatusMatricula(id, request);

        mockMvc.perform(patch("/alunos/{id}/matricula", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Aluno não encontrado."));

        verify(alunoService).atualizarStatusMatricula(id, request);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarConflict_QuandoStatusJaEstiverAtualizado() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new AlunoUpdateMatriculaRequest(true);

        doThrow(new MatriculaInvalidaException())
                .when(alunoService).atualizarStatusMatricula(id, request);

        mockMvc.perform(patch("/alunos/{id}/matricula", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A situação atual da matricula já está neste estado."));

        verify(alunoService).atualizarStatusMatricula(id, request);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveAtualizarDataMatriculaAluno_QuandoSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new AlunoUpdateDataMatriculaRequest(LocalDate.now().minusMonths(4));

        doNothing().when(alunoService).atualizarDataMatricula(id, request);

        mockMvc.perform(patch("/alunos/{id}/data-matricula", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(alunoService).atualizarDataMatricula(id, request);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveAtualizarDataNascimentoAluno_QuandoSucesso() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new AlunoUpdateDataNascimentoRequest(LocalDate.now().minusYears(20));

        doNothing().when(alunoService).atualizarDataNascimento(id, request);

        mockMvc.perform(patch("/alunos/{id}/data-nascimento", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(alunoService).atualizarDataNascimento(id, request);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarNotFound_QuandoAlunoNaoExistir_AoAtualizarDataNascimentoAluno() throws Exception {
        UUID id = UUID.randomUUID();
        var request = new AlunoUpdateDataNascimentoRequest(LocalDate.now().minusYears(20));

        doThrow(new AlunoNaoEncontradoException())
                .when(alunoService).atualizarDataNascimento(id, request);

        mockMvc.perform(patch("/alunos/{id}/data-nascimento", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Aluno não encontrado."));

        verify(alunoService).atualizarDataNascimento(id, request);
    }
}