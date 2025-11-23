// java
package com.fighthub.service;

import com.fighthub.dto.presenca.PresencaRequest;
import com.fighthub.exception.AulaNaoEncontradaException;
import com.fighthub.exception.InscricaoNaoEncontradaException;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.model.*;
import com.fighthub.model.enums.ClassStatus;
import com.fighthub.model.enums.Role;
import com.fighthub.model.enums.SubscriptionStatus;
import com.fighthub.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresencaServiceTest {

    @Mock
    private PresencaRepository presencaRepository;

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private InscricaoRepository inscricaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private PresencaService presencaService;

    private Aula aula;
    private Turma turma;
    private Professor professor;
    private Usuario professorUsuario;
    private Usuario alunoUsuario;
    private Aluno aluno;
    private Inscricao inscricao;
    private Presenca presenca;

    @BeforeEach
    void setUp() {
        professorUsuario = Usuario.builder().id(UUID.randomUUID()).role(Role.PROFESSOR).email("prof@example.com").build();
        professor = Professor.builder().id(UUID.randomUUID()).usuario(professorUsuario).build();
        turma = Turma.builder().id(UUID.randomUUID()).professor(professor).build();
        aula = Aula.builder()
                .id(UUID.randomUUID())
                .turma(turma)
                .data(LocalDateTime.now().plusHours(1))
                .status(ClassStatus.DISPONIVEL)
                .titulo("Aula X")
                .build();

        alunoUsuario = Usuario.builder().id(UUID.randomUUID()).role(Role.ALUNO).email("aluno@example.com").nome("Aluno teste").build();
        aluno = Aluno.builder().id(UUID.randomUUID()).usuario(alunoUsuario).build();

        inscricao = Inscricao.builder()
                .id(UUID.randomUUID())
                .aula(aula)
                .aluno(aluno)
                .status(SubscriptionStatus.INSCRITO)
                .build();

        presenca = Presenca.builder()
                .id(UUID.randomUUID())
                .inscricao(inscricao)
                .presente(true)
                .dataRegistro(LocalDate.now())
                .build();
    }

    @Test
    void deveRegistrarNovaPresencaComSucesso() {
        UUID inscricaoId = inscricao.getId();
        PresencaRequest request = new PresencaRequest(true);
        String token = "token-valido";

        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findById(inscricaoId)).thenReturn(Optional.of(inscricao));
        when(jwtService.extrairEmail(token)).thenReturn(professorUsuario.getEmail());
        when(usuarioRepository.findByEmail(professorUsuario.getEmail())).thenReturn(Optional.of(professorUsuario));
        when(professorRepository.findByUsuario(professorUsuario)).thenReturn(Optional.of(professor));
        when(turmaRepository.findAllByProfessor(professor)).thenReturn(List.of(turma));
        when(presencaRepository.findByInscricao(inscricao)).thenReturn(Optional.empty());

        presencaService.atualizarStatusPresencaPorInscricao(aula.getId(), inscricaoId, request, httpServletRequest);

        ArgumentCaptor<Presenca> captor = ArgumentCaptor.forClass(Presenca.class);
        verify(presencaRepository).save(captor.capture());
        Presenca saved = captor.getValue();
        assertEquals(inscricao, saved.getInscricao());
        assertTrue(saved.isPresente());
    }

    @Test
    void deveAtualizarPresencaExistenteComSucesso_AlterandoStatus() {
        UUID inscricaoId = inscricao.getId();
        PresencaRequest request = new PresencaRequest(false);
        String token = "token-valido";
        Presenca presencaExistente = Presenca.builder().id(UUID.randomUUID()).inscricao(inscricao).presente(true).build();

        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findById(inscricaoId)).thenReturn(Optional.of(inscricao));
        when(jwtService.extrairEmail(token)).thenReturn(professorUsuario.getEmail());
        when(usuarioRepository.findByEmail(professorUsuario.getEmail())).thenReturn(Optional.of(professorUsuario));
        when(professorRepository.findByUsuario(professorUsuario)).thenReturn(Optional.of(professor));
        when(turmaRepository.findAllByProfessor(professor)).thenReturn(List.of(turma));
        when(presencaRepository.findByInscricao(inscricao)).thenReturn(Optional.of(presencaExistente));

        presencaService.atualizarStatusPresencaPorInscricao(aula.getId(), inscricaoId, request, httpServletRequest);

        ArgumentCaptor<Presenca> captor = ArgumentCaptor.forClass(Presenca.class);
        verify(presencaRepository).save(captor.capture());
        Presenca saved = captor.getValue();
        assertEquals(inscricao, saved.getInscricao());
        assertFalse(saved.isPresente());
    }

    @Test
    void deveLancarExcecao_QuandoAulaNaoEncontrada_AoAtualizarPresenca() {
        UUID inscricaoId = inscricao.getId();
        PresencaRequest request = new PresencaRequest(true);
        String token = "token-valido";

        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.extrairEmail(token)).thenReturn(professorUsuario.getEmail());
        when(usuarioRepository.findByEmail(professorUsuario.getEmail())).thenReturn(Optional.of(professorUsuario));
        when(inscricaoRepository.findById(inscricaoId)).thenReturn(Optional.of(inscricao));
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.empty());

        var result = assertThrows(AulaNaoEncontradaException.class,
                () -> presencaService.atualizarStatusPresencaPorInscricao(aula.getId(), inscricaoId, request, httpServletRequest));

        assertNotNull(result);
        assertEquals("Aula não encontrada.", result.getMessage());
        verify(presencaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoInscricaoNaoEncontrada_AoAtualizarPresenca() {
        UUID inscricaoId = inscricao.getId();
        PresencaRequest request = new PresencaRequest(true);
        String token = "token-valido";

        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.extrairEmail(token)).thenReturn(professorUsuario.getEmail());
        when(usuarioRepository.findByEmail(professorUsuario.getEmail())).thenReturn(Optional.of(professorUsuario));
        when(inscricaoRepository.findById(inscricaoId)).thenReturn(Optional.empty());

        var result = assertThrows(InscricaoNaoEncontradaException.class,
                () -> presencaService.atualizarStatusPresencaPorInscricao(aula.getId(), inscricaoId, request, httpServletRequest));

        assertNotNull(result);
        assertEquals("Inscrição não encontrada.", result.getMessage());
        verify(presencaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioLogadoNaoEncontrado_AoAtualizarPresenca() {
        UUID inscricaoId = inscricao.getId();
        PresencaRequest request = new PresencaRequest(true);
        String token = "token-valido";

        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.extrairEmail(token)).thenReturn(professorUsuario.getEmail());
        when(usuarioRepository.findByEmail(professorUsuario.getEmail())).thenReturn(Optional.empty());

        var result = assertThrows(UsuarioNaoEncontradoException.class,
                () -> presencaService.atualizarStatusPresencaPorInscricao(aula.getId(), inscricaoId, request, httpServletRequest));

        assertNotNull(result);
        assertEquals("Usuário não encontrado.", result.getMessage());
        verify(presencaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoProfessorNaoDaAula_AoAtualizarPresenca() {
        UUID inscricaoId = inscricao.getId();
        PresencaRequest request = new PresencaRequest(true);
        String token = "token-valido";

        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findById(inscricaoId)).thenReturn(Optional.of(inscricao));
        when(jwtService.extrairEmail(token)).thenReturn(professorUsuario.getEmail());
        when(usuarioRepository.findByEmail(professorUsuario.getEmail())).thenReturn(Optional.of(professorUsuario));
        when(professorRepository.findByUsuario(professorUsuario)).thenReturn(Optional.of(professor));
        when(turmaRepository.findAllByProfessor(professor)).thenReturn(List.of());

        var result = assertThrows(ValidacaoException.class,
                () -> presencaService.atualizarStatusPresencaPorInscricao(aula.getId(), inscricaoId, request, httpServletRequest));

        assertNotNull(result);
        assertEquals("Professor não autorizado a registrar/cancelar presença para esta aula.", result.getMessage());
        verify(presencaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoInscricaoNaoPertenceAAula_AoAtualizarPresenca() {
        UUID inscricaoId = inscricao.getId();
        PresencaRequest request = new PresencaRequest(true);
        String token = "token-valido";

        inscricao.setAula(Aula.builder().id(UUID.randomUUID()).build());

        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findById(inscricaoId)).thenReturn(Optional.of(inscricao));
        when(jwtService.extrairEmail(token)).thenReturn(professorUsuario.getEmail());
        when(usuarioRepository.findByEmail(professorUsuario.getEmail())).thenReturn(Optional.of(professorUsuario));
        when(professorRepository.findByUsuario(professorUsuario)).thenReturn(Optional.of(professor));
        when(turmaRepository.findAllByProfessor(professor)).thenReturn(List.of(turma));

        var result = assertThrows(ValidacaoException.class,
                () -> presencaService.atualizarStatusPresencaPorInscricao(aula.getId(), inscricaoId, request, httpServletRequest));

        assertNotNull(result);
        assertEquals("Inscrição não pertence a esta aula.", result.getMessage());
        verify(presencaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoPresencaJaRegistradaComMesmoStatus_AoAtualizarPresenca() {
        UUID inscricaoId = inscricao.getId();
        PresencaRequest request = new PresencaRequest(true);
        String token = "token-valido";
        Presenca presencaExistente = Presenca.builder().id(UUID.randomUUID()).presente(true).inscricao(inscricao).build();

        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findById(inscricaoId)).thenReturn(Optional.of(inscricao));
        when(jwtService.extrairEmail(token)).thenReturn(professorUsuario.getEmail());
        when(usuarioRepository.findByEmail(professorUsuario.getEmail())).thenReturn(Optional.of(professorUsuario));
        when(professorRepository.findByUsuario(professorUsuario)).thenReturn(Optional.of(professor));
        when(turmaRepository.findAllByProfessor(professor)).thenReturn(List.of(turma));
        when(presencaRepository.findByInscricao(inscricao)).thenReturn(Optional.of(presencaExistente));

        var result = assertThrows(ValidacaoException.class,
                () -> presencaService.atualizarStatusPresencaPorInscricao(aula.getId(), inscricaoId, request, httpServletRequest));

        assertNotNull(result);
        assertEquals("Presença já registrada com o mesmo status.", result.getMessage());
        verify(presencaRepository, never()).save(any());
    }

    @Test
    void listarPresencasPorAula_noInscricoes_returnsEmptyPage() {
        String token = "token-prof";
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.extrairEmail(token)).thenReturn(professorUsuario.getEmail());
        when(usuarioRepository.findByEmail(professorUsuario.getEmail())).thenReturn(Optional.of(professorUsuario));
        when(professorRepository.findByUsuario(professorUsuario)).thenReturn(Optional.of(professor));
        when(turmaRepository.findAllByProfessor(professor)).thenReturn(List.of(turma));

        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findAllByAulaAndStatus(aula, SubscriptionStatus.INSCRITO)).thenReturn(List.of());

        when(presencaRepository.findAllByInscricaoIn(List.of(), PageRequest.of(0, 10)))
                .thenReturn(Page.empty(PageRequest.of(0, 10)));

        var result = presencaService.listarPresencasPorAula(aula.getId(), PageRequest.of(0, 10), httpServletRequest);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listarPresencasPorAula_professorAuthorized_returnsMappedPage() {
        String token = "token-prof";
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.extrairEmail(token)).thenReturn(professorUsuario.getEmail());
        when(usuarioRepository.findByEmail(professorUsuario.getEmail())).thenReturn(Optional.of(professorUsuario));
        when(professorRepository.findByUsuario(professorUsuario)).thenReturn(Optional.of(professor));
        when(turmaRepository.findAllByProfessor(professor)).thenReturn(List.of(turma));

        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findAllByAulaAndStatus(aula, SubscriptionStatus.INSCRITO)).thenReturn(List.of(inscricao));

        Page<Presenca> presencaPage = new PageImpl<>(List.of(presenca), PageRequest.of(0, 10), 1);
        when(presencaRepository.findAllByInscricaoIn(List.of(inscricao), PageRequest.of(0, 10))).thenReturn(presencaPage);

        var result = presencaService.listarPresencasPorAula(aula.getId(), PageRequest.of(0, 10), httpServletRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void listarPresencasPorAula_professorNotAuthorized_throwsValidacaoException() {
        String token = "token-prof";
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.extrairEmail(token)).thenReturn(professorUsuario.getEmail());
        when(usuarioRepository.findByEmail(professorUsuario.getEmail())).thenReturn(Optional.of(professorUsuario));
        when(professorRepository.findByUsuario(professorUsuario)).thenReturn(Optional.of(professor));
        when(turmaRepository.findAllByProfessor(professor)).thenReturn(List.of()); // not teaching this class

        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));

        var ex = assertThrows(ValidacaoException.class,
                () -> presencaService.listarPresencasPorAula(aula.getId(), PageRequest.of(0, 10), httpServletRequest));

        assertEquals("Professor não autorizado a verificar presenças para esta aula.", ex.getMessage());
    }

    @Test
    void listarMinhasPresencas_alunoWithPresencas_returnsMappedPage() {
        String token = "token-aluno";
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.extrairEmail(token)).thenReturn(alunoUsuario.getEmail());
        when(usuarioRepository.findByEmail(alunoUsuario.getEmail())).thenReturn(Optional.of(alunoUsuario));
        when(alunoRepository.findByUsuarioId(alunoUsuario.getId())).thenReturn(Optional.of(aluno));

        Page<Inscricao> inscricoesPage = new PageImpl<>(List.of(inscricao), PageRequest.of(0,10), 1);
        when(inscricaoRepository.findAllByAlunoAndStatus(aluno, SubscriptionStatus.INSCRITO, PageRequest.of(0,10)))
                .thenReturn(inscricoesPage);

        Page<Presenca> presencaPage = new PageImpl<>(List.of(presenca), PageRequest.of(0,10), 1);
        when(presencaRepository.findAllByInscricaoIn(List.of(inscricao), PageRequest.of(0,10)))
                .thenReturn(presencaPage);

        var result = presencaService.listarMinhasPresencas(PageRequest.of(0,10), httpServletRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void listarMinhasPresencas_notAluno_throwsValidacaoException() {
        String token = "token-prof";
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.extrairEmail(token)).thenReturn(professorUsuario.getEmail());
        when(usuarioRepository.findByEmail(professorUsuario.getEmail())).thenReturn(Optional.of(professorUsuario));

        var ex = assertThrows(ValidacaoException.class,
                () -> presencaService.listarMinhasPresencas(PageRequest.of(0, 10), httpServletRequest));

        assertEquals("Apenas alunos podem acessar suas presenças.", ex.getMessage());
    }

    @Test
    void listarMinhasPresencas_noInscricoes_returnsEmptyPage() {
        String token = "token-aluno";
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.extrairEmail(token)).thenReturn(alunoUsuario.getEmail());
        when(usuarioRepository.findByEmail(alunoUsuario.getEmail())).thenReturn(Optional.of(alunoUsuario));
        when(alunoRepository.findByUsuarioId(alunoUsuario.getId())).thenReturn(Optional.of(aluno));

        Page<Inscricao> emptyInscricoes = new PageImpl<>(List.of(), PageRequest.of(0,10), 0);
        when(inscricaoRepository.findAllByAlunoAndStatus(aluno, SubscriptionStatus.INSCRITO, PageRequest.of(0,10)))
                .thenReturn(emptyInscricoes);

        var result = presencaService.listarMinhasPresencas(PageRequest.of(0,10), httpServletRequest);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}