package com.fighthub.service;

import com.fighthub.dto.aluno.*;
import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.exception.AlunoNaoEncontradoException;
import com.fighthub.exception.CpfExistenteException;
import com.fighthub.exception.MatriculaInvalidaException;
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
import org.junit.jupiter.api.DisplayName;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {

    @InjectMocks
    private AlunoService alunoService;

    @Mock private AlunoRepository alunoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ResponsavelRepository responsavelRepository;
    @Mock private TokenService tokenService;
    @Mock private EmailService emailService;

    private CriarAlunoRequest criarAlunoRequest;
    private Usuario usuario;
    private Aluno aluno;

    @BeforeEach
    void setUp() {
        criarAlunoRequest = new CriarAlunoRequest(
                "João",
                "joao@email.com",
                "123.456.789-00",
                LocalDate.now().minusYears(17),
                List.of(UUID.randomUUID())
        );

        Endereco endereco = Endereco.builder()
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
                .nome(criarAlunoRequest.nome())
                .email(criarAlunoRequest.email())
                .cpf(criarAlunoRequest.cpf())
                .role(Role.ALUNO)
                .ativo(false)
                .loginSocial(false)
                .endereco(endereco)
                .build();

        aluno = Aluno.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .dataMatricula(LocalDate.now())
                .dataNascimento(criarAlunoRequest.dataNascimento())
                .matriculaAtiva(true)
                .responsaveis(new ArrayList<>())
                .build();
    }

    @Test
    void deveCriarAlunoMenorDeIdade_ComResponsaveis() {
        when(usuarioRepository.existsByEmail(criarAlunoRequest.email())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(alunoRepository.save(any(Aluno.class))).thenReturn(aluno);
        when(responsavelRepository.findAllById(any())).thenReturn(List.of(new Responsavel()));
        when(tokenService.salvarTokenAtivacao(usuario)).thenReturn("token-ativacao");

        alunoService.criarAluno(criarAlunoRequest);

        verify(usuarioRepository).save(any(Usuario.class));
        verify(alunoRepository, times(2)).save(any(Aluno.class));
        verify(emailService).enviarEmailAtivacao(usuario, "token-ativacao");
    }

    @Test
    void deveLancarExcecao_QuandoEmailJaCadastrado() {
        when(usuarioRepository.existsByEmail(criarAlunoRequest.email())).thenReturn(true);

        ValidacaoException ex = assertThrows(ValidacaoException.class,
                () -> alunoService.criarAluno(criarAlunoRequest));

        assertEquals("E-mail já cadastrado", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
        verify(emailService, never()).enviarEmailAtivacao(any(), any());
    }

    @Test
    void deveLancarExcecao_QuandoCpfJaCadastrado() {
        when(usuarioRepository.existsByEmail(criarAlunoRequest.email())).thenReturn(false);
        when(usuarioRepository.findByCpf(criarAlunoRequest.cpf())).thenReturn(Optional.of(usuario));

        var ex = assertThrows(CpfExistenteException.class,
                () -> alunoService.criarAluno(criarAlunoRequest));

        assertEquals("Usuário já existente com este CPF", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
        verify(emailService, never()).enviarEmailAtivacao(any(), any());
    }

    @Test
    void deveLancarExcecao_SeMenorDeIdadeSemResponsavel() {
        CriarAlunoRequest semResponsaveis = new CriarAlunoRequest(
                "João",
                "joao@email.com",
                "123.456.789-00",
                LocalDate.now().minusYears(16),
                List.of()
        );

        var ex = assertThrows(ValidacaoException.class,
                () -> alunoService.criarAluno(semResponsaveis));

        assertEquals("Aluno menor de idade deve ter ao menos um responsável", ex.getMessage());
    }

    @Test
    void deveCriarAlunoMaiorDeIdade_SemResponsaveis() {
        CriarAlunoRequest maior = new CriarAlunoRequest(
                "Ana",
                "ana@email.com",
                "123.456.789-00",
                LocalDate.now().minusYears(25),
                null
        );

        when(usuarioRepository.existsByEmail(maior.email())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(alunoRepository.save(any(Aluno.class))).thenReturn(aluno);
        when(tokenService.salvarTokenAtivacao(usuario)).thenReturn("token-ativacao");

        alunoService.criarAluno(maior);

        verify(alunoRepository, times(1)).save(any(Aluno.class));
        verify(responsavelRepository, never()).findAllById(any());
        verify(emailService).enviarEmailAtivacao(any(), eq("token-ativacao"));
    }

    @Test
    void deveRetornarPaginaDeAlunos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Aluno> page = new PageImpl<>(List.of(aluno));

        when(alunoRepository.findAll(pageable)).thenReturn(page);

        Page<AlunoResponse> result = alunoService.obterTodos(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(alunoRepository).findAll(pageable);
    }

    @Test
    void deveRetornarAlunoDetalhado() {
        when(alunoRepository.findById(aluno.getId())).thenReturn(Optional.of(aluno));

        var result = alunoService.obterAluno(aluno.getId());

        assertNotNull(result);
        assertEquals("João", result.nome());
        assertEquals("joao@email.com", result.email());
        verify(alunoRepository).findById(aluno.getId());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoExistir_AoBuscarAluno() {
        when(alunoRepository.findById(aluno.getId())).thenReturn(Optional.empty());

        var ex = assertThrows(AlunoNaoEncontradoException.class,
                () -> alunoService.obterAluno(aluno.getId()));

        assertNotNull(ex);
        assertEquals("Aluno não encontrado.", ex.getMessage());
        verify(alunoRepository).findById(aluno.getId());
    }

    @Test
    void deveAtualizarStatusMatriculaAluno_QuandoEstiverDiferenteDoRequest() {
        var alunoId = aluno.getId();
        var request = new AlunoUpdateMatriculaRequest(false);
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        alunoService.atualizarStatusMatricula(alunoId, request);

        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository).save(any());
        verify(alunoRepository).save(argThat(a -> !a.isMatriculaAtiva()));
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoExistir_AoAtualizarStatusMatricula() {
        var alunoId = aluno.getId();
        var request = new AlunoUpdateMatriculaRequest(false);
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        var ex = assertThrows(AlunoNaoEncontradoException.class,
                () -> alunoService.atualizarStatusMatricula(alunoId, request));

        assertNotNull(ex);
        assertEquals("Aluno não encontrado.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveAtualizarDataMatriculaAluno() {
        var alunoId = aluno.getId();
        var request = new AlunoUpdateDataMatriculaRequest(LocalDate.now().minusMonths(4));
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        alunoService.atualizarDataMatricula(alunoId, request);

        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoExistir_AoAtualizarDataMatriculaAluno() {
        var alunoId = aluno.getId();
        var request = new AlunoUpdateDataMatriculaRequest(LocalDate.now().minusMonths(4));
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        var ex = assertThrows(AlunoNaoEncontradoException.class,
                () -> alunoService.atualizarDataMatricula(alunoId, request));

        assertNotNull(ex);
        assertEquals("Aluno não encontrado.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveAtualizarDataNascimentoAluno() {
        var alunoId = aluno.getId();
        var request = new AlunoUpdateDataNascimentoRequest(LocalDate.now().minusYears(20));
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        alunoService.atualizarDataNascimento(alunoId, request);

        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoExistir_AoAtualizarDataNascimentoAluno() {
        var alunoId = aluno.getId();
        var request = new AlunoUpdateDataNascimentoRequest(LocalDate.now().minusYears(20));
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        var ex = assertThrows(AlunoNaoEncontradoException.class,
                () -> alunoService.atualizarDataNascimento(alunoId, request));

        assertNotNull(ex);
        assertEquals("Aluno não encontrado.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoStatusEstiverIgualAoRequest_AoAtualizarStatusMatricula() {
        var alunoId = aluno.getId();
        var request = new AlunoUpdateMatriculaRequest(false);
        aluno.setMatriculaAtiva(false);
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(MatriculaInvalidaException.class,
                () -> alunoService.atualizarStatusMatricula(alunoId, request));

        assertNotNull(ex);
        assertEquals("A situação atual da matricula já está neste estado.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

}