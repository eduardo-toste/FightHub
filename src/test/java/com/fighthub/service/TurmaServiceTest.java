package com.fighthub.service;

import com.fighthub.dto.turma.TurmaRequest;
import com.fighthub.dto.turma.TurmaUpdateCompletoRequest;
import com.fighthub.dto.turma.TurmaUpdateStatusRequest;
import com.fighthub.exception.ProfessorNaoEncontradoException;
import com.fighthub.exception.TurmaNaoEncontradaException;
import com.fighthub.model.Endereco;
import com.fighthub.model.Professor;
import com.fighthub.model.Turma;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.ProfessorRepository;
import com.fighthub.repository.TurmaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurmaServiceTest {

    @Mock private TurmaRepository turmaRepository;
    @Mock private ProfessorRepository professorRepository;

    @InjectMocks private TurmaService turmaService;

    private Endereco endereco;
    private Usuario usuario;
    private Professor professor;
    private Turma turma;

    @BeforeEach
    void setup() {
        endereco = Endereco.builder()
                .cep("12345-678")
                .logradouro("Rua das Flores")
                .numero("123")
                .complemento("Apto 45")
                .bairro("Centro")
                .cidade("São Paulo")
                .estado("SP")
                .build();

        usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Usuario Teste")
                .email("email@teste.com")
                .cpf("111.111.111-11")
                .role(Role.PROFESSOR)
                .ativo(false)
                .loginSocial(false)
                .endereco(endereco)
                .build();

        professor = Professor.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .build();

        turma = Turma.builder()
                .id(UUID.randomUUID())
                .nome("Turma de Segunda")
                .horario("Segunda 19:00")
                .professor(professor)
                .ativo(true)
                .build();
    }

    @Test
    void deveCriarTurmaComSucesso() {
        var request = new TurmaRequest("Turma de domingo", "Domingo 10:00", UUID.randomUUID());
        when(professorRepository.findById(request.professorId())).thenReturn(Optional.of(professor));

        turmaService.criarTurma(request);

        verify(professorRepository).findById(request.professorId());
        verify(turmaRepository).save(any());
    }

    @Test
    void deveLancarException_AoCriarTurma_QuandoProfessorNaoExistir() {
        var request = new TurmaRequest("Turma de domingo", "Domingo 10:00", UUID.randomUUID());
        when(professorRepository.findById(request.professorId())).thenReturn(Optional.empty());

        var ex = assertThrows(ProfessorNaoEncontradoException.class,
                () -> turmaService.criarTurma(request));

        assertNotNull(ex);
        assertEquals("Professor não encontrado.", ex.getMessage());

        verify(professorRepository).findById(request.professorId());
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveRetornarPagePopulada_AoBuscarTurmasComSucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Turma> page = new PageImpl<>(List.of(turma));
        when(turmaRepository.findAll(pageable)).thenReturn(page);

        var result = turmaService.buscarTurmas(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(turmaRepository).findAll(pageable);
    }

    @Test
    void deveRetornarPageVazia_AoBuscarTurmasComSucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Turma> page = new PageImpl<>(List.of());
        when(turmaRepository.findAll(pageable)).thenReturn(page);

        var result = turmaService.buscarTurmas(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(turmaRepository).findAll(pageable);
    }

    @Test
    void deveBuscarTurmaPorIdComSucesso() {
        var id = UUID.randomUUID();
        when(turmaRepository.findById(id)).thenReturn(Optional.of(turma));

        var result = turmaService.buscarTurmaPorId(id);

        assertNotNull(result);
        assertEquals("Turma de Segunda", result.nome());
        assertEquals("Segunda 19:00", result.horario());
        verify(turmaRepository).findById(id);
    }

    @Test
    void deveLancarExcecao_AoBuscarTurmaPorId_QuandoTurmaNaoExistir() {
        var id = UUID.randomUUID();
        when(turmaRepository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(TurmaNaoEncontradaException.class,
                () -> turmaService.buscarTurmaPorId(id));

        assertNotNull(ex);
        assertEquals("Turma não encontrada.", ex.getMessage());
        verify(turmaRepository).findById(id);
    }

    @Test
    void deveAtualizarTurmaComSucesso() {
        var idTurma = turma.getId();
        var request = new TurmaUpdateCompletoRequest("Turma Nova", "Horario Novo", professor.getId(), true);
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(professorRepository.findById(request.professorId())).thenReturn(Optional.of(professor));
        when(turmaRepository.save(any(Turma.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = turmaService.atualizarTurma(idTurma, request);

        assertNotNull(result);
        assertEquals("Turma Nova", result.nome());
        assertEquals("Horario Novo", result.horario());
        assertEquals(professor.getId(), result.professorId());
        assertTrue(result.ativo());

        verify(turmaRepository).findById(idTurma);
        verify(professorRepository).findById(any());
        verify(turmaRepository).save(any());
    }

    @Test
    void deveLancarExcecao_AoAtualizarTurma_QuandoTurmaNaoExistir() {
        var idTurma = turma.getId();
        var request = new TurmaUpdateCompletoRequest("Turma Nova", "Horario Novo", professor.getId(), true);
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.empty());

        var ex = assertThrows(TurmaNaoEncontradaException.class,
                () -> turmaService.atualizarTurma(idTurma, request));

        assertNotNull(ex);
        assertEquals("Turma não encontrada.", ex.getMessage());

        verify(turmaRepository).findById(idTurma);
        verify(professorRepository, never()).findById(any());
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_AoAtualizarTurma_QuandoProfessorNaoExistir() {
        var idTurma = turma.getId();
        var request = new TurmaUpdateCompletoRequest("Turma Nova", "Horario Novo", professor.getId(), true);
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(professorRepository.findById(request.professorId())).thenReturn(Optional.empty());

        var ex = assertThrows(ProfessorNaoEncontradoException.class,
                () -> turmaService.atualizarTurma(idTurma, request));

        assertNotNull(ex);
        assertEquals("Professor não encontrado.", ex.getMessage());

        verify(turmaRepository).findById(idTurma);
        verify(professorRepository).findById(request.professorId());
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveAtualizarStatusTurmaComSucesso() {
        var id = UUID.randomUUID();
        var request = new TurmaUpdateStatusRequest(false);
        when(turmaRepository.findById(id)).thenReturn(Optional.of(turma));
        when(turmaRepository.save(any(Turma.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = turmaService.atualizarStatusTurma(id, request);

        assertNotNull(result);
        assertFalse(result.ativo());

        verify(turmaRepository).findById(id);
        verify(turmaRepository).save(any());
    }

    @Test
    void deveLancarExcecao_AoAtualizarStatusTurma_QuandoTurmaNaoExistir() {
        var idTurma = turma.getId();
        var request = new TurmaUpdateStatusRequest(false);
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.empty());

        var ex = assertThrows(TurmaNaoEncontradaException.class,
                () -> turmaService.atualizarStatusTurma(idTurma, request));

        assertNotNull(ex);
        assertEquals("Turma não encontrada.", ex.getMessage());

        verify(turmaRepository).findById(idTurma);
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveExcluirTurmaComSucesso() {
        var id = UUID.randomUUID();
        when(turmaRepository.findById(id)).thenReturn(Optional.of(turma));

        turmaService.excluirTurma(id);

        verify(turmaRepository).findById(id);
        verify(turmaRepository).delete(any());
    }

    @Test
    void deveLancarExcecao_AoExcluirTurma_QuandoTurmaNaoExistir() {
        var idTurma = turma.getId();
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.empty());

        var ex = assertThrows(TurmaNaoEncontradaException.class,
                () -> turmaService.excluirTurma(idTurma));

        assertNotNull(ex);
        assertEquals("Turma não encontrada.", ex.getMessage());

        verify(turmaRepository).findById(idTurma);
        verify(turmaRepository, never()).delete(any());
    }

}