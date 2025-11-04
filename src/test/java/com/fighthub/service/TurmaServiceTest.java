package com.fighthub.service;

import com.fighthub.dto.turma.TurmaRequest;
import com.fighthub.dto.turma.TurmaUpdateCompletoRequest;
import com.fighthub.dto.turma.TurmaUpdateStatusRequest;
import com.fighthub.exception.*;
import com.fighthub.model.*;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.AlunoRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
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
    @Mock private AlunoRepository alunoRepository;

    @InjectMocks private TurmaService turmaService;

    private Endereco endereco;
    private Usuario usuario;
    private Professor professor;
    private Turma turma;
    private Aluno aluno;

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

        aluno = Aluno.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .dataMatricula(LocalDate.now())
                .dataNascimento(LocalDate.now().minusYears(20))
                .matriculaAtiva(true)
                .responsaveis(new ArrayList<>())
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
                .alunos(new ArrayList<>())
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

    @Test
    void deveVincularAlunoATurmaComSucesso() {
        var idTurma = UUID.randomUUID();
        var idAluno = UUID.randomUUID();
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(aluno));

        turmaService.vincularAluno(idTurma, idAluno);

        assertTrue(turma.getAlunos().contains(aluno));
        verify(turmaRepository).findById(idTurma);
        verify(alunoRepository).findById(idAluno);
        verify(turmaRepository).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoTurmaNaoEncontrado_AoVincularAlunoATurma() {
        var idTurma = UUID.randomUUID();
        var idAluno = UUID.randomUUID();
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.empty());

        var ex = assertThrows(TurmaNaoEncontradaException.class,
                () -> turmaService.vincularAluno(idTurma, idAluno));

        assertNotNull(ex);
        assertEquals("Turma não encontrada.", ex.getMessage());
        verify(turmaRepository).findById(idTurma);
        verify(alunoRepository, never()).findById(idAluno);
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoEncontrado_AoVincularAlunoATurma() {
        var idTurma = UUID.randomUUID();
        var idAluno = UUID.randomUUID();
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.empty());

        var ex = assertThrows(AlunoNaoEncontradoException.class,
                () -> turmaService.vincularAluno(idTurma, idAluno));

        assertNotNull(ex);
        assertEquals("Aluno não encontrado.", ex.getMessage());
        verify(turmaRepository).findById(idTurma);
        verify(alunoRepository).findById(idAluno);
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoJaEstiverVinculado_AoVincularAlunoATurma() {
        var idTurma = UUID.randomUUID();
        var idAluno = UUID.randomUUID();
        turma.getAlunos().add(aluno);
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(ValidacaoException.class,
                () -> turmaService.vincularAluno(idTurma, idAluno));

        assertNotNull(ex);
        assertEquals("Aluno já está vinculado à turma.", ex.getMessage());
        verify(turmaRepository).findById(idTurma);
        verify(alunoRepository).findById(idAluno);
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveVincularProfessorATurmaComSucesso() {
        var idTurma = UUID.randomUUID();
        var idProfessor = UUID.randomUUID();
        turma = Turma.builder()
                .id(UUID.randomUUID())
                .nome("Turma de Segunda")
                .horario("Segunda 19:00")
                .professor(null)
                .ativo(true)
                .alunos(new ArrayList<>())
                .build();
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(professorRepository.findById(idProfessor)).thenReturn(Optional.of(professor));

        turmaService.vincularProfessor(idTurma, idProfessor);

        assertEquals(turma.getProfessor(), professor);
        verify(turmaRepository).findById(idTurma);
        verify(professorRepository).findById(idProfessor);
        verify(turmaRepository).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoTurmaNaoEncontrada_AoVincularProfessorATurma() {
        var idTurma = UUID.randomUUID();
        var idProfessor = UUID.randomUUID();
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.empty());

        var ex = assertThrows(TurmaNaoEncontradaException.class,
                () -> turmaService.vincularProfessor(idTurma, idProfessor));

        assertNotNull(ex);
        assertEquals("Turma não encontrada.", ex.getMessage());
        verify(turmaRepository).findById(idTurma);
        verify(professorRepository, never()).findById(idProfessor);
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoProfessorNaoEncontrado_AoVincularProfessorATurma() {
        var idTurma = UUID.randomUUID();
        var idProfessor = UUID.randomUUID();
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(professorRepository.findById(idProfessor)).thenReturn(Optional.empty());

        var ex = assertThrows(ProfessorNaoEncontradoException.class,
                () -> turmaService.vincularProfessor(idTurma, idProfessor));

        assertNotNull(ex);
        assertEquals("Professor não encontrado.", ex.getMessage());
        verify(turmaRepository).findById(idTurma);
        verify(professorRepository).findById(idProfessor);
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoProfessorJaVinculado_AoVincularProfessorATurma() {
        var idTurma = UUID.randomUUID();
        var idProfessor = UUID.randomUUID();
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(professorRepository.findById(idProfessor)).thenReturn(Optional.of(professor));

        var ex = assertThrows(ValidacaoException.class,
                () -> turmaService.vincularProfessor(idTurma, idProfessor));

        assertNotNull(ex);
        assertEquals("Professor já está vinculado à turma.", ex.getMessage());
        verify(turmaRepository).findById(idTurma);
        verify(professorRepository).findById(idProfessor);
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveDesvincularProfessorDaTurmaComSucesso() {
        var idTurma = UUID.randomUUID();
        var idProfessor = professor.getId();
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(professorRepository.findById(idProfessor)).thenReturn(Optional.of(professor));

        turmaService.desvincularProfessor(idTurma, idProfessor);

        assertNull(turma.getProfessor());
        verify(turmaRepository).findById(idTurma);
        verify(professorRepository).findById(idProfessor);
        verify(turmaRepository).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoTurmaNaoEncontrada_AoDesvincularProfessor() {
        var idTurma = UUID.randomUUID();
        var idProfessor = UUID.randomUUID();
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.empty());

        var ex = assertThrows(TurmaNaoEncontradaException.class,
                () -> turmaService.desvincularProfessor(idTurma, idProfessor));

        assertEquals("Turma não encontrada.", ex.getMessage());
        verify(turmaRepository).findById(idTurma);
        verify(professorRepository, never()).findById(idProfessor);
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoProfessorNaoEncontrado_AoDesvincularProfessor() {
        var idTurma = UUID.randomUUID();
        var idProfessor = UUID.randomUUID();
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(professorRepository.findById(idProfessor)).thenReturn(Optional.empty());

        var ex = assertThrows(ProfessorNaoEncontradoException.class,
                () -> turmaService.desvincularProfessor(idTurma, idProfessor));

        assertEquals("Professor não encontrado.", ex.getMessage());
        verify(turmaRepository).findById(idTurma);
        verify(professorRepository).findById(idProfessor);
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoNaoHaProfessorVinculado_AoDesvincularProfessor() {
        var idTurma = UUID.randomUUID();
        var idProfessor = UUID.randomUUID();
        turma.setProfessor(null);
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(professorRepository.findById(idProfessor)).thenReturn(Optional.of(professor));

        var ex = assertThrows(ValidacaoException.class,
                () -> turmaService.desvincularProfessor(idTurma, idProfessor));

        assertEquals("Ainda não há professor vinculado à turma.", ex.getMessage());
        verify(turmaRepository).findById(idTurma);
        verify(professorRepository).findById(idProfessor);
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoProfessorNaoForOMesmoVinculado_AoDesvincularProfessor() {
        var idTurma = UUID.randomUUID();
        var outroProfessor = Professor.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .build();

        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(professorRepository.findById(outroProfessor.getId())).thenReturn(Optional.of(outroProfessor));

        var ex = assertThrows(ValidacaoException.class,
                () -> turmaService.desvincularProfessor(idTurma, outroProfessor.getId()));

        assertEquals("Professor não está vinculado à turma.", ex.getMessage());
        verify(turmaRepository).findById(idTurma);
        verify(professorRepository).findById(outroProfessor.getId());
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveDesvincularAlunoDaTurmaComSucesso() {
        var idTurma = UUID.randomUUID();
        var idAluno = aluno.getId();
        turma.getAlunos().add(aluno);

        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(aluno));

        turmaService.desvincularAluno(idTurma, idAluno);

        assertFalse(turma.getAlunos().contains(aluno));
        verify(turmaRepository).findById(idTurma);
        verify(alunoRepository).findById(idAluno);
        verify(turmaRepository).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoTurmaNaoEncontrada_AoDesvincularAluno() {
        var idTurma = UUID.randomUUID();
        var idAluno = UUID.randomUUID();
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.empty());

        var ex = assertThrows(TurmaNaoEncontradaException.class,
                () -> turmaService.desvincularAluno(idTurma, idAluno));

        assertEquals("Turma não encontrada.", ex.getMessage());
        verify(turmaRepository).findById(idTurma);
        verify(alunoRepository, never()).findById(idAluno);
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoEncontrado_AoDesvincularAluno() {
        var idTurma = UUID.randomUUID();
        var idAluno = UUID.randomUUID();
        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.empty());

        var ex = assertThrows(AlunoNaoEncontradoException.class,
                () -> turmaService.desvincularAluno(idTurma, idAluno));

        assertEquals("Aluno não encontrado.", ex.getMessage());
        verify(turmaRepository).findById(idTurma);
        verify(alunoRepository).findById(idAluno);
        verify(turmaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoEstiverVinculado_AoDesvincularAluno() {
        var idTurma = UUID.randomUUID();
        var idAluno = UUID.randomUUID();

        when(turmaRepository.findById(idTurma)).thenReturn(Optional.of(turma));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(ValidacaoException.class,
                () -> turmaService.desvincularAluno(idTurma, idAluno));

        assertEquals("Aluno não está vinculado à turma.", ex.getMessage());
        verify(turmaRepository).findById(idTurma);
        verify(alunoRepository).findById(idAluno);
        verify(turmaRepository, never()).save(any());
    }

}