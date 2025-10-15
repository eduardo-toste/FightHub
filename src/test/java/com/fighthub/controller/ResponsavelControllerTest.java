package com.fighthub.controller;

import com.fighthub.dto.endereco.EnderecoResponse;
import com.fighthub.dto.responsavel.CriarResponsavelRequest;
import com.fighthub.dto.responsavel.ResponsavelDetalhadoResponse;
import com.fighthub.dto.responsavel.ResponsavelResponse;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.exception.AlunoNaoEncontradoException;
import com.fighthub.exception.ResponsavelNaoEncontradoException;
import com.fighthub.service.ResponsavelService;
import com.fighthub.utils.ControllerTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResponsavelController.class)
class ResponsavelControllerTest extends ControllerTestBase {

    @MockBean
    private ResponsavelService responsavelService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveCriarResponsavel() throws Exception {
        var request = new CriarResponsavelRequest("Ana", "ana@email.com", "785.579.220-19");

        doNothing().when(responsavelService).criacaoResponsavel(request);

        mockMvc.perform(post("/responsaveis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(responsavelService).criacaoResponsavel(request);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveListarResponsaveis() throws Exception {
        var page = new PageImpl<>(List.of(
                new ResponsavelResponse(UUID.randomUUID(), "Ana", "ana@email.com", "(11)99999-0000", null)
        ));

        when(responsavelService.obterTodosResponsaveis(any())).thenReturn(page);

        mockMvc.perform(get("/responsaveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Ana"));

        verify(responsavelService).obterTodosResponsaveis(any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveBuscarResponsavelPorId() throws Exception {
        UUID id = UUID.randomUUID();
        var response = new ResponsavelDetalhadoResponse(
                id, "Ana", "ana@email.com", "(11)99999-0000", null,
                new EnderecoResponse("12345-677", "Rua da Flor", "113", "Apto 44", "Centro", "São Paulo", "SP")
        );

        when(responsavelService.obterResponsavelPorId(id)).thenReturn(response);

        mockMvc.perform(get("/responsaveis/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(responsavelService).obterResponsavelPorId(id);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarNotFound_QuandoBuscarResponsavelInexistente() throws Exception {
        UUID id = UUID.randomUUID();

        when(responsavelService.obterResponsavelPorId(id)).thenThrow(new ResponsavelNaoEncontradoException());

        mockMvc.perform(get("/responsaveis/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Responsavel não encontrado."));

        verify(responsavelService).obterResponsavelPorId(id);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveVincularAlunoAoResponsavel() throws Exception {
        UUID idResponsavel = UUID.randomUUID();
        UUID idAluno = UUID.randomUUID();

        doNothing().when(responsavelService).vincularAlunoAoResponsavel(idResponsavel, idAluno);

        mockMvc.perform(patch("/responsaveis/{idResponsavel}/alunos/{idAluno}", idResponsavel, idAluno))
                .andExpect(status().isOk());

        verify(responsavelService).vincularAlunoAoResponsavel(idResponsavel, idAluno);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarNotFound_QuandoAlunoOuResponsavelNaoExistirAoVincular() throws Exception {
        UUID idResponsavel = UUID.randomUUID();
        UUID idAluno = UUID.randomUUID();

        doThrow(new ResponsavelNaoEncontradoException()).when(responsavelService).vincularAlunoAoResponsavel(idResponsavel, idAluno);

        mockMvc.perform(patch("/responsaveis/{idResponsavel}/alunos/{idAluno}", idResponsavel, idAluno))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Responsavel não encontrado."));

        verify(responsavelService).vincularAlunoAoResponsavel(idResponsavel, idAluno);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarConflict_QuandoAlunoJaVinculado() throws Exception {
        UUID idResponsavel = UUID.randomUUID();
        UUID idAluno = UUID.randomUUID();

        doThrow(new ValidacaoException("Vínculo de responsabilidade já estabelecido.")).when(responsavelService).vincularAlunoAoResponsavel(idResponsavel, idAluno);

        mockMvc.perform(patch("/responsaveis/{idResponsavel}/alunos/{idAluno}", idResponsavel, idAluno))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Vínculo de responsabilidade já estabelecido."));

        verify(responsavelService).vincularAlunoAoResponsavel(idResponsavel, idAluno);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveDesvincularAlunoDeResponsavel() throws Exception {
        UUID idResponsavel = UUID.randomUUID();
        UUID idAluno = UUID.randomUUID();

        doNothing().when(responsavelService).removerVinculoAlunoEResponsavel(idResponsavel, idAluno);

        mockMvc.perform(delete("/responsaveis/{idResponsavel}/alunos/{idAluno}", idResponsavel, idAluno))
                .andExpect(status().isOk());

        verify(responsavelService).removerVinculoAlunoEResponsavel(idResponsavel, idAluno);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarNotFound_QuandoAlunoNaoExistirAoDesvincular() throws Exception {
        UUID idResponsavel = UUID.randomUUID();
        UUID idAluno = UUID.randomUUID();

        doThrow(new AlunoNaoEncontradoException()).when(responsavelService).removerVinculoAlunoEResponsavel(idResponsavel, idAluno);

        mockMvc.perform(delete("/responsaveis/{idResponsavel}/alunos/{idAluno}", idResponsavel, idAluno))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Aluno não encontrado."));

        verify(responsavelService).removerVinculoAlunoEResponsavel(idResponsavel, idAluno);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deveRetornarConflict_QuandoVinculoNaoExistirAoDesvincular() throws Exception {
        UUID idResponsavel = UUID.randomUUID();
        UUID idAluno = UUID.randomUUID();

        doThrow(new ValidacaoException("Responsável não vinculado ao aluno.")).when(responsavelService).removerVinculoAlunoEResponsavel(idResponsavel, idAluno);

        mockMvc.perform(delete("/responsaveis/{idResponsavel}/alunos/{idAluno}", idResponsavel, idAluno))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Responsável não vinculado ao aluno."));

        verify(responsavelService).removerVinculoAlunoEResponsavel(idResponsavel, idAluno);
    }
}