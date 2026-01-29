package com.fighthub.service;

import com.fighthub.dto.inscricao.InscricaoResponse;
import com.fighthub.exception.AlunoNaoEncontradoException;
import com.fighthub.exception.AulaNaoEncontradaException;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.mapper.InscricaoMapper;
import com.fighthub.model.Aluno;
import com.fighthub.model.Aula;
import com.fighthub.model.Inscricao;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.ClassStatus;
import com.fighthub.model.enums.SubscriptionStatus;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.repository.AulaRepository;
import com.fighthub.repository.InscricaoRepository;
import com.fighthub.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscricaoServiceTest {

    @Mock
    private InscricaoRepository inscricaoRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private InscricaoService inscricaoService;

    private Aula aula;
    private Aluno aluno;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // provide sensible defaults so verificaDisponibilidadeInscricao won't NPE
        aula = Aula.builder()
                .id(UUID.randomUUID())
                .data(LocalDateTime.now().plusHours(2))
                .status(ClassStatus.DISPONIVEL)
                .build();

        aluno = Aluno.builder().id(UUID.randomUUID()).build();
        usuario = Usuario.builder().id(UUID.randomUUID()).email("user@example.com").build();

        // make repository.save return the saved instance for Inscricao
        lenient().when(inscricaoRepository.save(any(Inscricao.class))).thenAnswer((Answer<Inscricao>) invocation -> invocation.getArgument(0));
    }

    private HttpServletRequest authRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        lenient().when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token");
        return request;
    }

    @Test
    void deveLancarExcecao_QuandoAlunoJaInscrito_AoInscrever() {
        UUID aulaId = aula.getId();
        Inscricao inscricao = new Inscricao(aluno, aula, SubscriptionStatus.INSCRITO, LocalDateTime.now());

        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aula));
        when(jwtService.extrairEmail(anyString())).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(aluno));
        when(inscricaoRepository.findByAulaAndAluno(aula, aluno)).thenReturn(Optional.of(inscricao));

        HttpServletRequest request = authRequest();

        ValidacaoException ex = assertThrows(ValidacaoException.class,
                () -> inscricaoService.inscreverAluno(aulaId, request));
        assertEquals("Aluno já inscrito na aula.", ex.getMessage());
        verify(inscricaoRepository, never()).save(any());
    }

    @Test
    void deveReativarInscricaoCancelada_AoInscrever() {
        UUID aulaId = aula.getId();
        Inscricao inscricao = new Inscricao(aluno, aula, SubscriptionStatus.CANCELADO, LocalDateTime.of(2020, 1, 1, 0, 0));

        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aula));
        when(jwtService.extrairEmail(anyString())).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(aluno));
        when(inscricaoRepository.findByAulaAndAluno(aula, aluno)).thenReturn(Optional.of(inscricao));

        HttpServletRequest request = authRequest();

        inscricaoService.inscreverAluno(aulaId, request);

        ArgumentCaptor<Inscricao> captor = ArgumentCaptor.forClass(Inscricao.class);
        verify(inscricaoRepository).save(captor.capture());
        Inscricao saved = captor.getValue();

        assertEquals(SubscriptionStatus.INSCRITO, saved.getStatus());
        assertTrue(saved.getInscritoEm().toLocalDate().isEqual(LocalDateTime.now().toLocalDate()));
    }

    @Test
    void deveCriarInscricaoQuandoNaoExistir_AoInscrever() {
        UUID aulaId = aula.getId();

        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aula));
        when(jwtService.extrairEmail(anyString())).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(aluno));
        when(inscricaoRepository.findByAulaAndAluno(aula, aluno)).thenReturn(Optional.empty());

        HttpServletRequest request = authRequest();

        inscricaoService.inscreverAluno(aulaId, request);

        ArgumentCaptor<Inscricao> captor = ArgumentCaptor.forClass(Inscricao.class);
        verify(inscricaoRepository).save(captor.capture());
        Inscricao created = captor.getValue();

        assertEquals(SubscriptionStatus.INSCRITO, created.getStatus());
        assertTrue(created.getInscritoEm().toLocalDate().isEqual(LocalDateTime.now().toLocalDate()));
        assertNotNull(created.getAluno());
        assertNotNull(created.getAula());
    }

    @Test
    void deveLancarExcecao_QuandoNaoInscrito_AoCancelar() {
        UUID aulaId = aula.getId();

        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aula));
        when(jwtService.extrairEmail(anyString())).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(aluno));
        when(inscricaoRepository.findByAulaAndAluno(aula, aluno)).thenReturn(Optional.empty());

        HttpServletRequest request = authRequest();

        ValidacaoException ex = assertThrows(ValidacaoException.class,
                () -> inscricaoService.cancelarInscricao(aulaId, request));
        assertEquals("Aluno não está inscrito na aula.", ex.getMessage());
        verify(inscricaoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoJaCancelada_AoCancelar() {
        UUID aulaId = aula.getId();
        Inscricao inscricao = new Inscricao(aluno, aula, SubscriptionStatus.CANCELADO, LocalDateTime.now());

        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aula));
        when(jwtService.extrairEmail(anyString())).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(aluno));
        when(inscricaoRepository.findByAulaAndAluno(aula, aluno)).thenReturn(Optional.of(inscricao));

        HttpServletRequest request = authRequest();

        ValidacaoException ex = assertThrows(ValidacaoException.class,
                () -> inscricaoService.cancelarInscricao(aulaId, request));
        assertEquals("Inscrição já está cancelada.", ex.getMessage());
        verify(inscricaoRepository, never()).save(any());
    }

    @Test
    void deveCancelarInscricaoComSucesso() {
        UUID aulaId = aula.getId();
        Inscricao inscricao = new Inscricao(aluno, aula, SubscriptionStatus.INSCRITO, LocalDateTime.now());

        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aula));
        when(jwtService.extrairEmail(anyString())).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(aluno));
        when(inscricaoRepository.findByAulaAndAluno(aula, aluno)).thenReturn(Optional.of(inscricao));

        HttpServletRequest request = authRequest();

        inscricaoService.cancelarInscricao(aulaId, request);

        ArgumentCaptor<Inscricao> captor = ArgumentCaptor.forClass(Inscricao.class);
        verify(inscricaoRepository).save(captor.capture());
        Inscricao saved = captor.getValue();
        assertEquals(SubscriptionStatus.CANCELADO, saved.getStatus());
    }

    @Test
    void deveLancarExcecao_QuandoAulaNaoExistir_AoBuscarInscricoesPorAula() {
        UUID aulaId = UUID.randomUUID();
        when(aulaRepository.findById(aulaId)).thenReturn(Optional.empty());

        assertThrows(AulaNaoEncontradaException.class,
                () -> inscricaoService.buscarInscricoesPorAula(aulaId, Pageable.unpaged()));
        verify(inscricaoRepository, never()).findAllByAula(any(), any());
    }

    @Test
    void deveRetornarPaginaMapeada_AoBuscarInscricoesPorAula() {
        UUID aulaId = aula.getId();
        Page<Inscricao> page = new PageImpl<>(List.of());
        Page<InscricaoResponse> expected = new PageImpl<>(List.of());

        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findAllByAula(aula, Pageable.unpaged())).thenReturn(page);

        try (MockedStatic<InscricaoMapper> mocked = mockStatic(InscricaoMapper.class)) {
            mocked.when(() -> InscricaoMapper.toPageDTO(page)).thenReturn(expected);

            Page<InscricaoResponse> result = inscricaoService.buscarInscricoesPorAula(aulaId, Pageable.unpaged());
            assertSame(expected, result);
            mocked.verify(() -> InscricaoMapper.toPageDTO(page));
        }
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioNaoExistir_AoBuscarInscricoesProprias() {
        HttpServletRequest request = authRequest();
        when(jwtService.extrairEmail(anyString())).thenReturn("nonexistent@example.com");
        when(usuarioRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(UsuarioNaoEncontradoException.class,
                () -> inscricaoService.buscarInscricoesProprias(request, Pageable.unpaged()));
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoExistir_AoBuscarInscricoesProprias() {
        HttpServletRequest request = authRequest();
        when(jwtService.extrairEmail(anyString())).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.empty());

        assertThrows(AlunoNaoEncontradoException.class,
                () -> inscricaoService.buscarInscricoesProprias(request, Pageable.unpaged()));
    }

    @Test
    void deveRetornarPaginaMapeada_AoBuscarInscricoesProprias() {
        HttpServletRequest request = authRequest();
        when(jwtService.extrairEmail(anyString())).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(aluno));

        Page<Inscricao> page = new PageImpl<>(List.of());
        Page<InscricaoResponse> expected = new PageImpl<>(List.of());
        when(inscricaoRepository.findAllByAlunoAndStatus(aluno, SubscriptionStatus.INSCRITO, Pageable.unpaged()))
                .thenReturn(page);

        try (MockedStatic<InscricaoMapper> mocked = mockStatic(InscricaoMapper.class)) {
            mocked.when(() -> InscricaoMapper.toPageDTO(page)).thenReturn(expected);

            Page<InscricaoResponse> result = inscricaoService.buscarInscricoesProprias(request, Pageable.unpaged());
            assertSame(expected, result);
            mocked.verify(() -> InscricaoMapper.toPageDTO(page));
        }
    }

    @Test
    void deveLancarAulaNaoEncontrada_AoInscrever() {
        UUID aulaId = UUID.randomUUID();
        when(aulaRepository.findById(aulaId)).thenReturn(Optional.empty());
        HttpServletRequest request = authRequest();

        assertThrows(AulaNaoEncontradaException.class,
                () -> inscricaoService.inscreverAluno(aulaId, request));
    }

    @Test
    void deveLancarUsuarioNaoEncontrado_AoInscrever() {
        UUID aulaId = aula.getId();
        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aula));
        when(jwtService.extrairEmail(anyString())).thenReturn("no@user");
        when(usuarioRepository.findByEmail("no@user")).thenReturn(Optional.empty());

        HttpServletRequest request = authRequest();

        assertThrows(UsuarioNaoEncontradoException.class,
                () -> inscricaoService.inscreverAluno(aulaId, request));
    }

    @Test
    void deveLancarAlunoNaoEncontrado_AoInscrever() {
        UUID aulaId = aula.getId();
        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aula));
        when(jwtService.extrairEmail(anyString())).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.empty());

        HttpServletRequest request = authRequest();

        assertThrows(AlunoNaoEncontradoException.class,
                () -> inscricaoService.inscreverAluno(aulaId, request));
    }

    @Test
    void deveLancarExcecao_QuandoInscricoesEncerradas_AoInscrever() {
        UUID aulaId = UUID.randomUUID();
        Aula aulaSoon = Aula.builder()
                .id(aulaId)
                .data(LocalDateTime.now().plusMinutes(30))
                .status(ClassStatus.DISPONIVEL)
                .build();

        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aulaSoon));
        when(jwtService.extrairEmail(anyString())).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(aluno));

        HttpServletRequest request = authRequest();

        ValidacaoException ex = assertThrows(ValidacaoException.class,
                () -> inscricaoService.inscreverAluno(aulaId, request));
        assertEquals("Inscrições para esta aula estão encerradas.", ex.getMessage());
    }

    @Test
    void deveLancarExcecao_QuandoAulaNaoDisponivel_AoInscrever() {
        UUID aulaId = UUID.randomUUID();
        Aula aulaMock = mock(Aula.class);

        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aulaMock));
        when(aulaMock.getData()).thenReturn(LocalDateTime.now().plusHours(2)); // passes time check
        when(aulaMock.getStatus()).thenReturn(null); // not DISPONIVEL -> should fail
        when(jwtService.extrairEmail(anyString())).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(aluno));
        when(inscricaoRepository.findByAulaAndAluno(aulaMock, aluno)).thenReturn(Optional.empty());

        HttpServletRequest request = authRequest();

        ValidacaoException ex = assertThrows(ValidacaoException.class,
                () -> inscricaoService.inscreverAluno(aulaId, request));
        assertEquals("Aula não está disponível para inscrições.", ex.getMessage());
    }

    @Test
    void deveLancarAulaNaoEncontrada_AoCancelar() {
        UUID aulaId = UUID.randomUUID();
        when(aulaRepository.findById(aulaId)).thenReturn(Optional.empty());
        HttpServletRequest request = authRequest();

        assertThrows(AulaNaoEncontradaException.class,
                () -> inscricaoService.cancelarInscricao(aulaId, request));
    }

    @Test
    void deveLancarUsuarioNaoEncontrado_AoCancelar() {
        UUID aulaId = aula.getId();
        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aula));
        when(jwtService.extrairEmail(anyString())).thenReturn("no@user");
        when(usuarioRepository.findByEmail("no@user")).thenReturn(Optional.empty());

        HttpServletRequest request = authRequest();

        assertThrows(UsuarioNaoEncontradoException.class,
                () -> inscricaoService.cancelarInscricao(aulaId, request));
    }

    @Test
    void deveLancarAlunoNaoEncontrado_AoCancelar() {
        UUID aulaId = aula.getId();
        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aula));
        when(jwtService.extrairEmail(anyString())).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.empty());

        HttpServletRequest request = authRequest();

        assertThrows(AlunoNaoEncontradoException.class,
                () -> inscricaoService.cancelarInscricao(aulaId, request));
    }

    @Test
    void deveBloquearReativacao_QuandoInscricoesEncerradas() {
        UUID aulaId = aula.getId();
        Inscricao inscricao = new Inscricao(aluno, aula, SubscriptionStatus.CANCELADO, LocalDateTime.of(2020, 1, 1, 0, 0));
        Aula aulaSoon = Aula.builder()
                .id(aulaId)
                .data(LocalDateTime.now().plusMinutes(30))
                .status(ClassStatus.DISPONIVEL)
                .build();

        when(aulaRepository.findById(aulaId)).thenReturn(Optional.of(aulaSoon));
        when(jwtService.extrairEmail(anyString())).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(alunoRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(aluno));
        when(inscricaoRepository.findByAulaAndAluno(aulaSoon, aluno)).thenReturn(Optional.of(inscricao));

        HttpServletRequest request = authRequest();

        ValidacaoException ex = assertThrows(ValidacaoException.class,
                () -> inscricaoService.inscreverAluno(aulaId, request));
        assertEquals("Inscrições para esta aula estão encerradas.", ex.getMessage());
    }

}
