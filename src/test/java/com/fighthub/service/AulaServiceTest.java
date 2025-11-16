package com.fighthub.service;

import com.fighthub.dto.aula.AulaRequest;
import com.fighthub.dto.aula.AulaResponse;
import com.fighthub.dto.aula.AulaUpdateCompletoRequest;
import com.fighthub.dto.aula.AulaUpdateStatusRequest;
import com.fighthub.exception.*;
import com.fighthub.model.*;
import com.fighthub.model.enums.ClassStatus;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.repository.AulaRepository;
import com.fighthub.repository.TurmaRepository;
import com.fighthub.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AulaServiceTest {

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AulaService aulaService;

    private Endereco endereco;
    private Usuario usuario;
    private Turma turma;
    private Aluno aluno;
    private Aula aula;

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

        turma = Turma.builder()
                .id(UUID.randomUUID())
                .nome("Turma de Segunda")
                .horario("Segunda 19:00")
                .ativo(true)
                .alunos(new ArrayList<>())
                .build();

        aula = Aula.builder()
                .id(UUID.randomUUID())
                .titulo("Aula Teste")
                .descricao("Descricao Teste")
                .data(LocalDateTime.now().plusDays(2))
                .turma(turma)
                .limiteAlunos(10)
                .status(ClassStatus.DISPONIVEL)
                .ativo(true)
                .build();
    }

    @Test
    void deveCriarAulaComSucesso_QuandoTurmaPresente() {
        var request = new AulaRequest(
                "Aula Teste",
                "Descricao Teste",
                LocalDateTime.now().plusDays(2),
                turma.getId(),
                10
        );
        when(turmaRepository.findById(turma.getId())).thenReturn(Optional.of(turma));

        aulaService.criarAula(request);

        verify(turmaRepository).findById(any());
        verify(aulaRepository).save(any());
    }

    @Test
    void deveCriarAulaComSucesso_QuandoTurmaAusente() {
        var request = new AulaRequest(
                "Aula Teste",
                "Descricao Teste",
                LocalDateTime.now().plusDays(2),
                null,
                10
        );

        aulaService.criarAula(request);

        verify(aulaRepository).save(any());
    }

    @Test
    void deveRetornarPagePopulada_AoBuscarAulasComSucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Aula> page = new PageImpl<>(List.of(aula));
        when(aulaRepository.findAll(pageable)).thenReturn(page);

        var result = aulaService.buscarAulas(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(aulaRepository).findAll(pageable);
    }

    @Test
    void deveRetornarPageVazia_AoBuscarAulasComSucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Aula> page = new PageImpl<>(List.of());
        when(aulaRepository.findAll(pageable)).thenReturn(page);

        var result = aulaService.buscarAulas(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(aulaRepository).findAll(pageable);
    }

    @Test
    void deveRetornarAulasDisponiveis_QuandoAlunoEstaMatriculado() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Aula> page = new PageImpl<>(List.of(aula));
        String jwt = "jwt-valido";
        String email = "email-valido";

        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + jwt);
        when(jwtService.extrairEmail(jwt)).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(aluno));
        when(turmaRepository.findAllByAlunos(aluno)).thenReturn(List.of(turma));
        when(aulaRepository.findByStatusAndTurmaIn(ClassStatus.DISPONIVEL, List.of(turma), pageable))
                .thenReturn(page);

        var result = aulaService.buscarAulasDisponiveisAluno(pageable, httpServletRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        AulaResponse aulaResponse = result.getContent().get(0);
        assertEquals(aula.getTitulo(), aulaResponse.titulo());
        assertEquals(aula.getDescricao(), aulaResponse.descricao());
        assertEquals(aula.getData(), aulaResponse.data());

        verify(jwtService).extrairEmail(jwt);
        verify(usuarioRepository).findByEmail(email);
        verify(alunoRepository).findByUsuarioId(usuario.getId());
        verify(turmaRepository).findAllByAlunos(aluno);
        verify(aulaRepository).findByStatusAndTurmaIn(ClassStatus.DISPONIVEL, List.of(turma), pageable);
    }

    @Test
    void deveLancarUsuarioNaoEncontradoException_QuandoBuscarAulasDisponiveisPorEmailInexistente() {
        Pageable pageable = PageRequest.of(0, 10);
        String jwt = "jwt-invalido";
        String email = "email-nao-existe";

        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + jwt);
        when(jwtService.extrairEmail(jwt)).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsuarioNaoEncontradoException.class,
                () -> aulaService.buscarAulasDisponiveisAluno(pageable, httpServletRequest));

        verify(usuarioRepository).findByEmail(email);
        verifyNoInteractions(alunoRepository, turmaRepository, aulaRepository);
    }

    @Test
    void deveLancarAlunoNaoEncontradoException_QuandoBuscarAulasDisponiveisComUsuarioSemAluno() {
        Pageable pageable = PageRequest.of(0, 10);
        String jwt = "jwt-valido";
        String email = "email-valido";

        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + jwt);
        when(jwtService.extrairEmail(jwt)).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.empty());

        assertThrows(AlunoNaoEncontradoException.class,
                () -> aulaService.buscarAulasDisponiveisAluno(pageable, httpServletRequest));

        verify(alunoRepository).findByUsuarioId(usuario.getId());
        verifyNoInteractions(turmaRepository, aulaRepository);
    }

    @Test
    void deveRetornarPaginaVazia_QuandoAlunoNaoPossuiTurmasMatriculadas() {
        Pageable pageable = PageRequest.of(0, 10);
        String jwt = "jwt-valido";
        String email = "email-valido";

        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + jwt);
        when(jwtService.extrairEmail(jwt)).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(aluno));
        when(turmaRepository.findAllByAlunos(aluno)).thenReturn(List.of());
        when(aulaRepository.findByStatusAndTurmaIn(ClassStatus.DISPONIVEL, List.of(), pageable))
                .thenReturn(Page.empty());

        var result = aulaService.buscarAulasDisponiveisAluno(pageable, httpServletRequest);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(aulaRepository).findByStatusAndTurmaIn(ClassStatus.DISPONIVEL, List.of(), pageable);
    }

    @Test
    void deveRetornarAulaResponse_QuandoBuscarAulaPorIdComSucesso() {
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));

        AulaResponse response = aulaService.buscarAulaPorId(aula.getId());

        assertNotNull(response);
        assertEquals(aula.getId(), response.id());
        verify(aulaRepository).findById(aula.getId());
    }

    @Test
    void deveLancarExcecao_QuandoBuscarAulaPorIdInexistente() {
        UUID idInvalido = UUID.randomUUID();
        when(aulaRepository.findById(idInvalido)).thenReturn(Optional.empty());

        assertThrows(AulaNaoEncontradaException.class,
                () -> aulaService.buscarAulaPorId(idInvalido));
    }

    @Test
    void deveAtualizarAulaComSucesso() {
        AulaUpdateCompletoRequest request = new AulaUpdateCompletoRequest(
                "Novo Título", "Nova descrição", LocalDateTime.now().plusDays(10), turma.getId(), 15, true);

        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(turmaRepository.findById(turma.getId())).thenReturn(Optional.of(turma));
        when(aulaRepository.save(any(Aula.class))).thenReturn(aula);

        AulaResponse response = aulaService.atualizarAula(request, aula.getId());

        assertNotNull(response);
        assertEquals(request.titulo(), response.titulo());
        verify(aulaRepository).save(any(Aula.class));
    }

    @Test
    void deveLancarExcecao_QuandoAtualizarAulaComIdInexistente() {
        AulaUpdateCompletoRequest request = new AulaUpdateCompletoRequest(
                "Novo Título", "Nova descrição", LocalDateTime.now(), turma.getId(), 10, true);

        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.empty());

        assertThrows(AulaNaoEncontradaException.class,
                () -> aulaService.atualizarAula(request, aula.getId()));
    }

    @Test
    void deveLancarExcecao_QuandoTurmaNaoForEncontradaNaAtualizacao() {
        AulaUpdateCompletoRequest request = new AulaUpdateCompletoRequest(
                "Novo Título", "Nova descrição", LocalDateTime.now(), turma.getId(), 10, true);

        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(turmaRepository.findById(turma.getId())).thenReturn(Optional.empty());

        assertThrows(TurmaNaoEncontradaException.class,
                () -> aulaService.atualizarAula(request, aula.getId()));
    }

    @Test
    void deveAtualizarStatusComSucesso() {
        var request = new AulaUpdateStatusRequest(ClassStatus.FINALIZADA);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(aulaRepository.save(any())).thenReturn(aula);

        AulaResponse response = aulaService.atualizarStatus(aula.getId(), request);

        assertNotNull(response);
        assertEquals(request.status(), response.status());
        verify(aulaRepository).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAtualizarStatusDeAulaInexistente() {
        var request = new AulaUpdateStatusRequest(ClassStatus.CANCELADA);
        when(aulaRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(AulaNaoEncontradaException.class,
                () -> aulaService.atualizarStatus(UUID.randomUUID(), request));
    }

    @Test
    void deveVincularTurmaComSucesso() {
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(turmaRepository.findById(turma.getId())).thenReturn(Optional.of(turma));

        aula.setTurma(null);
        aulaService.vincularTurma(aula.getId(), turma.getId());

        verify(aulaRepository).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoTurmaJaEstiverVinculada() {
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(turmaRepository.findById(turma.getId())).thenReturn(Optional.of(turma));

        aula.setTurma(turma);

        assertThrows(ValidacaoException.class,
                () -> aulaService.vincularTurma(aula.getId(), turma.getId()));
    }

    @Test
    void deveDesvincularTurmaComSucesso() {
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(turmaRepository.findById(turma.getId())).thenReturn(Optional.of(turma));

        aula.setTurma(turma);

        aulaService.desvincularTurma(aula.getId(), turma.getId());

        verify(aulaRepository).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoTurmaNaoEstiverVinculadaNaAula() {
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(turmaRepository.findById(turma.getId())).thenReturn(Optional.of(turma));

        aula.setTurma(null);

        assertThrows(ValidacaoException.class,
                () -> aulaService.desvincularTurma(aula.getId(), turma.getId()));
    }

    @Test
    void deveExcluirAulaComSucesso() {
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));

        aulaService.excluirAula(aula.getId());

        verify(aulaRepository).deleteById(aula.getId());
    }

    @Test
    void deveLancarExcecao_QuandoExcluirAulaInexistente() {
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.empty());

        assertThrows(AulaNaoEncontradaException.class,
                () -> aulaService.excluirAula(aula.getId()));
    }

}