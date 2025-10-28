package com.fighthub.service;

import com.fighthub.dto.professor.CriarProfessorRequest;
import com.fighthub.exception.CpfExistenteException;
import com.fighthub.exception.ProfessorNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.model.Endereco;
import com.fighthub.model.Professor;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.ProfessorRepository;
import com.fighthub.repository.UsuarioRepository;
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
class ProfessorServiceTest {

    @Mock private ProfessorRepository professorRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TokenService tokenService;
    @Mock private EmailService emailService;

    @InjectMocks private ProfessorService professorService;

    private Usuario usuario;
    private Endereco endereco;
    private Professor professor;

    @BeforeEach
    void setUp() {
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
                .role(Role.ALUNO)
                .ativo(false)
                .loginSocial(false)
                .endereco(endereco)
                .build();

        professor = Professor.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .build();
    }

    @Test
    void deveCriarProfessorComSucesso() {
        var request = new CriarProfessorRequest(
                "Professor",
                "professor@teste.com",
                "709.982.350-75");
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(usuarioRepository.findByCpf(request.cpf())).thenReturn(Optional.empty());

        professorService.criacaoProfessor(request);

        verify(usuarioRepository).existsByEmail(request.email());
        verify(usuarioRepository).findByCpf(request.cpf());
        verify(usuarioRepository).save(any());
        verify(professorRepository).save(any());
        verify(tokenService).salvarTokenAtivacao(any());
        verify(emailService).enviarEmailAtivacao(any(), any());
    }

    @Test
    void deveLancarExcecao_QuandoEmailJaCadastrado_AoCriarProfessor() {
        var request = new CriarProfessorRequest(
                "Professor",
                "professor@teste.com",
                "709.982.350-75");
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(true);

        var ex = assertThrows(ValidacaoException.class,
                () -> professorService.criacaoProfessor(request));

        assertNotNull(ex);
        assertEquals("E-mail já cadastrado", ex.getMessage());
        verify(usuarioRepository, never()).findByCpf(request.cpf());
        verify(usuarioRepository, never()).save(any());
        verify(professorRepository, never()).save(any());
        verify(tokenService, never()).salvarTokenAtivacao(any());
        verify(emailService, never()).enviarEmailAtivacao(any(), any());
    }

    @Test
    void deveLancarExcecao_QuandoCpfJaCadastrado_AoCriarProfessor() {
        var request = new CriarProfessorRequest(
                "Professor",
                "professor@teste.com",
                "709.982.350-75");
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(usuarioRepository.findByCpf(request.cpf())).thenReturn(Optional.of(usuario));

        var ex = assertThrows(CpfExistenteException.class,
                () -> professorService.criacaoProfessor(request));

        assertNotNull(ex);
        assertEquals("Usuário já existente com este CPF", ex.getMessage());
        verify(usuarioRepository).existsByEmail(request.email());
        verify(usuarioRepository).findByCpf(request.cpf());
        verify(usuarioRepository, never()).save(any());
        verify(professorRepository, never()).save(any());
        verify(tokenService, never()).salvarTokenAtivacao(any());
        verify(emailService, never()).enviarEmailAtivacao(any(), any());
    }

    @Test
    void deveRetornarPageDeProfessoresComSucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Professor> page = new PageImpl<>(List.of(professor));
        when(professorRepository.findAll(pageable)).thenReturn(page);

        var result = professorService.buscarProfessores(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(professorRepository).findAll(pageable);
    }

    @Test
    void deveRetornarPageVaziaDeProfessoresComSucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Professor> page = new PageImpl<>(List.of());
        when(professorRepository.findAll(pageable)).thenReturn(page);

        var result = professorService.buscarProfessores(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(professorRepository).findAll(pageable);
    }

    @Test
    void deveRetornarProfessorBuscarPorId() {
        var id = UUID.randomUUID();
        when(professorRepository.findById(id)).thenReturn(Optional.of(professor));

        var result = professorService.buscarProfessorPorId(id);

        assertNotNull(result);
        assertEquals("Usuario Teste", result.nome());
        assertEquals("email@teste.com", result.email());
        verify(professorRepository).findById(id);
    }

    @Test
    void deveLancarExcecao_QuandoProfessorNaoEncontrado_AoBuscarProfessorPorId() {
        var id = UUID.randomUUID();
        when(professorRepository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(ProfessorNaoEncontradoException.class,
                () -> professorService.buscarProfessorPorId(id));

        assertNotNull(ex);
        assertEquals("Professor não encontrado.", ex.getMessage());
        verify(professorRepository).findById(id);
    }

}