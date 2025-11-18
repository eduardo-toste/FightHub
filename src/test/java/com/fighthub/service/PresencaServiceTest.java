package com.fighthub.service;

import com.fighthub.dto.presenca.PresencaRequest;
import com.fighthub.exception.AulaNaoEncontradaException;
import com.fighthub.exception.InscricaoNaoEncontradaException;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.model.*;
import com.fighthub.model.enums.ClassStatus;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

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
    private JwtService jwtService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private PresencaService presencaService;

    private Aula aula;
    private Professor professor;
    private Usuario usuario;
    private Inscricao inscricao;
    private Turma turma;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(UUID.randomUUID()).role(Role.PROFESSOR).email("user@example.com").build();
        professor = Professor.builder().id(UUID.randomUUID()).build();
        turma = Turma.builder().id(UUID.randomUUID()).professor(professor).build();
        aula = Aula.builder()
                .id(UUID.randomUUID())
                .turma(turma)
                .data(LocalDateTime.now().plusHours(2))
                .status(ClassStatus.DISPONIVEL)
                .build();
        inscricao = Inscricao.builder().id(UUID.randomUUID()).aula(aula).build();
    }

    @Test
    void deveRegistrarNovaPresencaComSucesso() {
        PresencaRequest request = new PresencaRequest(inscricao.getId(), true);
        String token = "token-valido";
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findById(request.inscricaoId())).thenReturn(Optional.of(inscricao));
        when(jwtService.extrairEmail(token)).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(professorRepository.findByUsuario(usuario)).thenReturn(Optional.of(professor));
        when(turmaRepository.findAllByProfessor(professor)).thenReturn(java.util.List.of(turma));
        when(presencaRepository.findByInscricao(inscricao)).thenReturn(Optional.empty());

        presencaService.registrarPresenca(aula.getId(), request, httpServletRequest);

        ArgumentCaptor<Presenca> captor = ArgumentCaptor.forClass(Presenca.class);

        verify(presencaRepository).save(captor.capture());
        Presenca saved = captor.getValue();
        assertEquals(inscricao, saved.getInscricao());
        assertTrue(saved.isPresente());
    }

    @Test
    void deveRegistrarPresencaExistenteComSucesso() {
        PresencaRequest request = new PresencaRequest(inscricao.getId(), true);
        String token = "token-valido";
        Presenca presenca = Presenca.builder().id(UUID.randomUUID()).inscricao(inscricao).build();
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findById(request.inscricaoId())).thenReturn(Optional.of(inscricao));
        when(jwtService.extrairEmail(token)).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(professorRepository.findByUsuario(usuario)).thenReturn(Optional.of(professor));
        when(turmaRepository.findAllByProfessor(professor)).thenReturn(java.util.List.of(turma));
        when(presencaRepository.findByInscricao(inscricao)).thenReturn(Optional.of(presenca));

        presencaService.registrarPresenca(aula.getId(), request, httpServletRequest);

        ArgumentCaptor<Presenca> captor = ArgumentCaptor.forClass(Presenca.class);

        verify(presencaRepository).save(captor.capture());
        Presenca saved = captor.getValue();
        assertEquals(inscricao, saved.getInscricao());
        assertTrue(saved.isPresente());
    }

    @Test
    void deveLancarExcecao_QuandoAulaNaoEncontrada_AoRegistrarPresenca() {
        PresencaRequest request = new PresencaRequest(inscricao.getId(), true);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.empty());

        var result = assertThrows(AulaNaoEncontradaException.class,
                () -> presencaService.registrarPresenca(aula.getId(), request, httpServletRequest));

        assertNotNull(result);
        assertEquals("Aula não encontrada.", result.getMessage());
        verify(presencaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoInscricaoNaoEncontrada_AoRegistrarPresenca() {
        PresencaRequest request = new PresencaRequest(inscricao.getId(), true);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findById(request.inscricaoId())).thenReturn(Optional.empty());

        var result = assertThrows(InscricaoNaoEncontradaException.class,
                () -> presencaService.registrarPresenca(aula.getId(), request, httpServletRequest));

        assertNotNull(result);
        assertEquals("Inscrição não encontrada.", result.getMessage());
        verify(presencaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioLogadoNaoEncontrado_AoRegistrarPresenca() {
        PresencaRequest request = new PresencaRequest(inscricao.getId(), true);
        String token = "token-valido";
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findById(request.inscricaoId())).thenReturn(Optional.of(inscricao));
        when(jwtService.extrairEmail(token)).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());

        var result = assertThrows(UsuarioNaoEncontradoException.class,
                () -> presencaService.registrarPresenca(aula.getId(), request, httpServletRequest));

        assertNotNull(result);
        assertEquals("Usuário não encontrado.", result.getMessage());
        verify(presencaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioLogadoNaoForProfessorNemAdmin_AoRegistrarPresenca() {
        PresencaRequest request = new PresencaRequest(inscricao.getId(), true);
        String token = "token-valido";
        usuario.setRole(Role.ALUNO);
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findById(request.inscricaoId())).thenReturn(Optional.of(inscricao));
        when(jwtService.extrairEmail(token)).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        var result = assertThrows(ValidacaoException.class,
                () -> presencaService.registrarPresenca(aula.getId(), request, httpServletRequest));

        assertNotNull(result);
        assertEquals("Professor não autorizado a registrar presença para esta aula.", result.getMessage());
        verify(presencaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoProfessorNaoDaAula_AoRegistrarPresenca() {
        PresencaRequest request = new PresencaRequest(inscricao.getId(), true);
        String token = "token-valido";
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findById(request.inscricaoId())).thenReturn(Optional.of(inscricao));
        when(jwtService.extrairEmail(token)).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(professorRepository.findByUsuario(usuario)).thenReturn(Optional.of(professor));
        when(turmaRepository.findAllByProfessor(professor)).thenReturn(List.of());

        var result = assertThrows(ValidacaoException.class,
                () -> presencaService.registrarPresenca(aula.getId(), request, httpServletRequest));

        assertNotNull(result);
        assertEquals("Professor não autorizado a registrar presença para esta aula.", result.getMessage());
        verify(presencaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoInscricaoNaoPertenceAAula_AoRegistrarPresenca() {
        PresencaRequest request = new PresencaRequest(inscricao.getId(), true);
        String token = "token-valido";
        inscricao.setAula(Aula.builder().build());
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findById(request.inscricaoId())).thenReturn(Optional.of(inscricao));
        when(jwtService.extrairEmail(token)).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(professorRepository.findByUsuario(usuario)).thenReturn(Optional.of(professor));
        when(turmaRepository.findAllByProfessor(professor)).thenReturn(List.of(turma));

        var result = assertThrows(ValidacaoException.class,
                () -> presencaService.registrarPresenca(aula.getId(), request, httpServletRequest));

        assertNotNull(result);
        assertEquals("Inscrição não pertence a esta aula.", result.getMessage());
        verify(presencaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoPresencaoJaRegistrada_AoRegistrarPresenca() {
        PresencaRequest request = new PresencaRequest(inscricao.getId(), true);
        String token = "token-valido";
        Presenca presenca = Presenca.builder().id(UUID.randomUUID()).presente(true).inscricao(inscricao).build();
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(aulaRepository.findById(aula.getId())).thenReturn(Optional.of(aula));
        when(inscricaoRepository.findById(request.inscricaoId())).thenReturn(Optional.of(inscricao));
        when(jwtService.extrairEmail(token)).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(professorRepository.findByUsuario(usuario)).thenReturn(Optional.of(professor));
        when(turmaRepository.findAllByProfessor(professor)).thenReturn(List.of(turma));
        when(presencaRepository.findByInscricao(inscricao)).thenReturn(Optional.of(presenca));

        var result = assertThrows(ValidacaoException.class,
                () -> presencaService.registrarPresenca(aula.getId(), request, httpServletRequest));

        assertNotNull(result);
        assertEquals("Presença já registrada com o mesmo status.", result.getMessage());
        verify(presencaRepository, never()).save(any());
    }

}