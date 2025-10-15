package com.fighthub.service;

import com.fighthub.dto.responsavel.CriarResponsavelRequest;
import com.fighthub.exception.AlunoNaoEncontradoException;
import com.fighthub.exception.CpfExistenteException;
import com.fighthub.exception.ResponsavelNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.model.Aluno;
import com.fighthub.model.Endereco;
import com.fighthub.model.Responsavel;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.repository.ResponsavelRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResponsavelServiceTest {

    @Mock
    private ResponsavelRepository responsavelRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ResponsavelService responsavelService;

    private Usuario usuario;
    private Endereco endereco;
    private Responsavel responsavel;
    private Aluno aluno;

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

        responsavel = Responsavel.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .alunos(new ArrayList<>())
                .build();

        aluno = Aluno.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .dataMatricula(LocalDate.now())
                .dataNascimento(LocalDate.now().minusYears(20))
                .matriculaAtiva(true)
                .responsaveis(new ArrayList<>())
                .build();
    }

    @Test
    void deveCriarResponsavelComSucesso() {
        var request = new CriarResponsavelRequest(
                "Responsavel",
                "responsavel@teste.com",
                "709.982.350-75");
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(usuarioRepository.findByCpf(request.cpf())).thenReturn(Optional.empty());

        responsavelService.criacaoResponsavel(request);

        verify(usuarioRepository).existsByEmail(request.email());
        verify(usuarioRepository).findByCpf(request.cpf());
        verify(usuarioRepository).save(any());
        verify(responsavelRepository).save(any());
        verify(tokenService).salvarTokenAtivacao(any());
        verify(emailService).enviarEmailAtivacao(any(), any());
    }

    @Test
    void deveLancarExcecao_QuandoEmailJaCadastrado_AoCriarResponsavel() {
        var request = new CriarResponsavelRequest(
                "Responsavel",
                "responsavel@teste.com",
                "709.982.350-75");
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(true);

        var ex = assertThrows(ValidacaoException.class,
                () -> responsavelService.criacaoResponsavel(request));

        assertNotNull(ex);
        assertEquals("E-mail já cadastrado", ex.getMessage());
        verify(usuarioRepository, never()).findByCpf(request.cpf());
        verify(usuarioRepository, never()).save(any());
        verify(responsavelRepository, never()).save(any());
        verify(tokenService, never()).salvarTokenAtivacao(any());
        verify(emailService, never()).enviarEmailAtivacao(any(), any());
    }

    @Test
    void deveLancarExcecao_QuandoCpfJaCadastrado_AoCriarResponsavel() {
        var request = new CriarResponsavelRequest(
                "Responsavel",
                "responsavel@teste.com",
                "709.982.350-75");
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(usuarioRepository.findByCpf(request.cpf())).thenReturn(Optional.of(usuario));

        var ex = assertThrows(CpfExistenteException.class,
                () -> responsavelService.criacaoResponsavel(request));

        assertNotNull(ex);
        assertEquals("Usuário já existente com este CPF", ex.getMessage());
        verify(usuarioRepository).existsByEmail(request.email());
        verify(usuarioRepository).findByCpf(request.cpf());
        verify(usuarioRepository, never()).save(any());
        verify(responsavelRepository, never()).save(any());
        verify(tokenService, never()).salvarTokenAtivacao(any());
        verify(emailService, never()).enviarEmailAtivacao(any(), any());
    }

    @Test
    void deveRetornarPageDeResponsaveisComSucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Responsavel> page = new PageImpl<>(List.of(responsavel));
        when(responsavelRepository.findAll(pageable)).thenReturn(page);

        var result = responsavelService.obterTodosResponsaveis(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(responsavelRepository).findAll(pageable);
    }

    @Test
    void deveRetornarPageVaziaDeResponsaveisComSucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Responsavel> page = new PageImpl<>(List.of());
        when(responsavelRepository.findAll(pageable)).thenReturn(page);

        var result = responsavelService.obterTodosResponsaveis(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(responsavelRepository).findAll(pageable);
    }

    @Test
    void deveRetornarResponsavelBuscarPorId() {
        var id = UUID.randomUUID();
        when(responsavelRepository.findById(id)).thenReturn(Optional.of(responsavel));

        var result = responsavelService.obterResponsavelPorId(id);

        assertNotNull(result);
        assertEquals("Usuario Teste", result.nome());
        assertEquals("email@teste.com", result.email());
        verify(responsavelRepository).findById(id);
    }

    @Test
    void deveLancarExcecao_QuandoResponsavelNaoEncontrado_AoBuscarResponsavelPorId() {
        var id = UUID.randomUUID();
        when(responsavelRepository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponsavelNaoEncontradoException.class,
                () -> responsavelService.obterResponsavelPorId(id));

        assertNotNull(ex);
        assertEquals("Responsavel não encontrado.", ex.getMessage());
        verify(responsavelRepository).findById(id);
    }

    @Test
    void deveVincularAlunoAoResponsavelComSucesso() {
        var idResponsavel = UUID.randomUUID();
        var idAluno = UUID.randomUUID();
        when(responsavelRepository.findById(idResponsavel)).thenReturn(Optional.of(responsavel));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(aluno));

        responsavelService.vincularAlunoAoResponsavel(idResponsavel, idAluno);

        assertTrue(responsavel.getAlunos().contains(aluno));
        verify(responsavelRepository).findById(idResponsavel);
        verify(alunoRepository).findById(idAluno);
        verify(responsavelRepository).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoResponsavelNaoEncontrado_AoVincularAlunoAoResponsavel() {
        var idResponsavel = UUID.randomUUID();
        var idAluno = UUID.randomUUID();
        when(responsavelRepository.findById(idResponsavel)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponsavelNaoEncontradoException.class,
                () -> responsavelService.vincularAlunoAoResponsavel(idResponsavel, idAluno));

        assertNotNull(ex);
        assertEquals("Responsavel não encontrado.", ex.getMessage());
        verify(responsavelRepository).findById(idResponsavel);
        verify(alunoRepository, never()).findById(idAluno);
        verify(responsavelRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoEncontrado_AoVincularAlunoAoResponsavel() {
        var idResponsavel = UUID.randomUUID();
        var idAluno = UUID.randomUUID();
        when(responsavelRepository.findById(idResponsavel)).thenReturn(Optional.of(responsavel));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.empty());

        var ex = assertThrows(AlunoNaoEncontradoException.class,
                () -> responsavelService.vincularAlunoAoResponsavel(idResponsavel, idAluno));

        assertNotNull(ex);
        assertEquals("Aluno não encontrado.", ex.getMessage());
        verify(responsavelRepository).findById(idResponsavel);
        verify(alunoRepository).findById(idAluno);
        verify(responsavelRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoJaEstiverVinculado_AoVincularAlunoAoResponsavel() {
        var idResponsavel = UUID.randomUUID();
        var idAluno = UUID.randomUUID();
        responsavel.getAlunos().add(aluno);
        when(responsavelRepository.findById(idResponsavel)).thenReturn(Optional.of(responsavel));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(ValidacaoException.class,
                () -> responsavelService.vincularAlunoAoResponsavel(idResponsavel, idAluno));

        assertNotNull(ex);
        assertEquals("Vínculo de responsabilidade já estabelecido.", ex.getMessage());
        verify(responsavelRepository).findById(idResponsavel);
        verify(alunoRepository).findById(idAluno);
        verify(responsavelRepository, never()).save(any());
    }

    @Test
    void deveDesvincularAlunoAoResponsavelComSucesso() {
        var idResponsavel = UUID.randomUUID();
        var idAluno = UUID.randomUUID();
        responsavel.getAlunos().add(aluno);
        when(responsavelRepository.findById(idResponsavel)).thenReturn(Optional.of(responsavel));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(aluno));

        responsavelService.removerVinculoAlunoEResponsavel(idResponsavel, idAluno);

        assertFalse(responsavel.getAlunos().contains(aluno));
        verify(responsavelRepository).findById(idResponsavel);
        verify(alunoRepository).findById(idAluno);
        verify(responsavelRepository).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoResponsavelNaoEncontrado_AoDesvincularAlunoAoResponsavel() {
        var idResponsavel = UUID.randomUUID();
        var idAluno = UUID.randomUUID();
        when(responsavelRepository.findById(idResponsavel)).thenReturn(Optional.empty());

        var ex = assertThrows(ResponsavelNaoEncontradoException.class,
                () -> responsavelService.removerVinculoAlunoEResponsavel(idResponsavel, idAluno));

        assertNotNull(ex);
        assertEquals("Responsavel não encontrado.", ex.getMessage());
        verify(responsavelRepository).findById(idResponsavel);
        verify(alunoRepository, never()).findById(idAluno);
        verify(responsavelRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoEncontrado_AoDesvincularAlunoAoResponsavel() {
        var idResponsavel = UUID.randomUUID();
        var idAluno = UUID.randomUUID();
        when(responsavelRepository.findById(idResponsavel)).thenReturn(Optional.of(responsavel));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.empty());

        var ex = assertThrows(AlunoNaoEncontradoException.class,
                () -> responsavelService.removerVinculoAlunoEResponsavel(idResponsavel, idAluno));

        assertNotNull(ex);
        assertEquals("Aluno não encontrado.", ex.getMessage());
        verify(responsavelRepository).findById(idResponsavel);
        verify(alunoRepository).findById(idAluno);
        verify(responsavelRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoTiverVinculo_AoDesvincularAlunoAoResponsavel() {
        var idResponsavel = UUID.randomUUID();
        var idAluno = UUID.randomUUID();
        when(responsavelRepository.findById(idResponsavel)).thenReturn(Optional.of(responsavel));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(ValidacaoException.class,
                () -> responsavelService.removerVinculoAlunoEResponsavel(idResponsavel, idAluno));

        assertNotNull(ex);
        assertEquals("Responsável não vinculado ao aluno.", ex.getMessage());
        verify(responsavelRepository).findById(idResponsavel);
        verify(alunoRepository).findById(idAluno);
        verify(responsavelRepository, never()).save(any());
    }

}