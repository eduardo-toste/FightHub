package com.fighthub.service;

import com.fighthub.dto.aluno.AlunoResponse;
import com.fighthub.dto.aluno.AlunoUpdateCompletoRequest;
import com.fighthub.dto.aluno.AlunoUpdateParcialRequest;
import com.fighthub.dto.aluno.CriarAlunoRequest;
import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.exception.UsuarioNaoEncontradoException;
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
        verify(alunoRepository, times(2)).save(any(Aluno.class)); // segunda vez após adicionar responsáveis
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

        verify(alunoRepository, times(1)).save(any(Aluno.class)); // apenas uma vez
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

        var ex = assertThrows(UsuarioNaoEncontradoException.class,
                () -> alunoService.obterAluno(aluno.getId()));

        assertNotNull(ex);
        assertEquals("Usuário não encontrado.", ex.getMessage());
        verify(alunoRepository).findById(aluno.getId());
    }

    @Test
    void deveAtualizarAlunoCompletamente() {
        var enderecoRequest = new EnderecoRequest(
                "12345-678",
                "Rua das Flores",
                "123",
                "Apto 45",
                "Centro",
                "São Paulo",
                "SP"
        );

        var responsavelId = UUID.randomUUID();
        var request = new AlunoUpdateCompletoRequest(
                "João Atualizado",
                "joao@email.com",
                null,
                "(11)12345-6789",
                LocalDate.now().minusYears(17),
                List.of(responsavelId),
                enderecoRequest
        );

        var usuarioResponsavel = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Maria Responsável")
                .email("maria@email.com")
                .cpf("987.654.321-00")
                .role(Role.RESPONSAVEL)
                .ativo(true)
                .build();

        var responsavel = Responsavel.builder()
                .id(responsavelId)
                .usuario(usuarioResponsavel)
                .build();

        var alunoExistente = aluno;

        when(alunoRepository.findById(alunoExistente.getId())).thenReturn(Optional.of(alunoExistente));
        when(responsavelRepository.findAllById(List.of(responsavelId))).thenReturn(List.of(responsavel));

        var result = alunoService.updateAlunoCompleto(alunoExistente.getId(), request);

        assertNotNull(result);
        assertEquals("João Atualizado", result.nome());
        assertEquals("(11)12345-6789", result.telefone());
        assertEquals("12345-678", result.endereco().cep());
        assertEquals("São Paulo", result.endereco().cidade());
        assertEquals("SP", result.endereco().estado());

        verify(alunoRepository).findById(alunoExistente.getId());
        verify(responsavelRepository).findAllById(List.of(responsavelId));
        verify(usuarioRepository).save(alunoExistente.getUsuario());
    }

    @Test
    void deveAtualizarAlunoCompletamente_QuandoForDeMaiorSemResponsavel() {
        var enderecoRequest = new EnderecoRequest(
                "12345-678",
                "Rua das Flores",
                "123",
                "Apto 45",
                "Centro",
                "São Paulo",
                "SP"
        );

        var request = new AlunoUpdateCompletoRequest(
                "João Atualizado",
                "joao@email.com",
                null,
                "(11)12345-6789",
                LocalDate.now().minusYears(20),
                null,
                enderecoRequest
        );

        var alunoExistente = aluno;

        when(alunoRepository.findById(alunoExistente.getId())).thenReturn(Optional.of(alunoExistente));

        var result = alunoService.updateAlunoCompleto(alunoExistente.getId(), request);

        assertNotNull(result);
        assertEquals("João Atualizado", result.nome());
        assertEquals("(11)12345-6789", result.telefone());
        assertEquals("12345-678", result.endereco().cep());
        assertEquals("São Paulo", result.endereco().cidade());
        assertEquals("SP", result.endereco().estado());

        verify(alunoRepository).findById(alunoExistente.getId());
        verify(usuarioRepository).save(alunoExistente.getUsuario());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoExistir_AoAtualizarAlunoCompletamente() {
        var enderecoRequest = new EnderecoRequest(
                "12345-678",
                "Rua das Flores",
                "123",
                "Apto 45",
                "Centro",
                "São Paulo",
                "SP"
        );

        var responsavelId = UUID.randomUUID();
        var request = new AlunoUpdateCompletoRequest(
                "João Atualizado",
                "joao@email.com",
                null,
                "(11)12345-6789",
                LocalDate.now().minusYears(17),
                List.of(responsavelId),
                enderecoRequest
        );
        when(alunoRepository.findById(aluno.getId())).thenReturn(Optional.empty());

        var ex = assertThrows(UsuarioNaoEncontradoException.class,
                () -> alunoService.updateAlunoCompleto(aluno.getId(), request));

        assertNotNull(ex);
        assertEquals("Usuário não encontrado.", ex.getMessage());
        verify(alunoRepository).findById(aluno.getId());
    }

    @Test
    void deveAtualizarAlunoCompletamente_SemResponsaveisListaVazia() {
        var enderecoRequest = new EnderecoRequest(
                "12345-678",
                "Rua das Flores",
                "123",
                "Apto 45",
                "Centro",
                "São Paulo",
                "SP"
        );

        var request = new AlunoUpdateCompletoRequest(
                "João Atualizado",
                "joao@email.com",
                null,
                "(11)12345-6789",
                LocalDate.now().minusYears(20),
                List.of(),
                enderecoRequest
        );

        when(alunoRepository.findById(aluno.getId())).thenReturn(Optional.of(aluno));

        var result = alunoService.updateAlunoCompleto(aluno.getId(), request);

        assertNotNull(result);
        assertEquals("João Atualizado", result.nome());
        verify(alunoRepository).findById(aluno.getId());
        verify(responsavelRepository, never()).findAllById(any());
        verify(usuarioRepository).save(aluno.getUsuario());
    }

    @Test
    void deveAtualizarAlunoCompletamente_SemResponsaveisNulos() {
        var enderecoRequest = new EnderecoRequest(
                "12345-678",
                "Rua das Flores",
                "123",
                "Apto 45",
                "Centro",
                "São Paulo",
                "SP"
        );

        var request = new AlunoUpdateCompletoRequest(
                "João Atualizado",
                "joao@email.com",
                null,
                "(11)12345-6789",
                LocalDate.now().minusYears(20),
                null, // <<< null
                enderecoRequest
        );

        when(alunoRepository.findById(aluno.getId())).thenReturn(Optional.of(aluno));

        var result = alunoService.updateAlunoCompleto(aluno.getId(), request);

        assertNotNull(result);
        assertEquals("João Atualizado", result.nome());
        verify(alunoRepository).findById(aluno.getId());
        verify(responsavelRepository, never()).findAllById(any());
        verify(usuarioRepository).save(aluno.getUsuario());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoMenorDeIdadeNaoTiverResponsavel_AoAtualizarAlunoCompletamente() {
        var enderecoRequest = new EnderecoRequest(
                "12345-678",
                "Rua das Flores",
                "123",
                "Apto 45",
                "Centro",
                "São Paulo",
                "SP"
        );

        var request = new AlunoUpdateCompletoRequest(
                "João Atualizado",
                "joao@email.com",
                null,
                "(11)12345-6789",
                LocalDate.now().minusYears(17),
                null,
                enderecoRequest
        );

        var alunoExistente = aluno;

        when(alunoRepository.findById(alunoExistente.getId())).thenReturn(Optional.of(alunoExistente));

        var ex = assertThrows(ValidacaoException.class,
                () -> alunoService.updateAlunoCompleto(aluno.getId(), request));

        assertNotNull(ex);
        assertEquals("Aluno menor de idade deve ter ao menos um responsável", ex.getMessage());
        verify(alunoRepository).findById(aluno.getId());
    }

    @Test
    void deveDesativarAluno_QuandoAlunoExistir() {
        var alunoId = aluno.getId();
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        alunoService.desativarAluno(alunoId);

        assertFalse(aluno.getUsuario().isAtivo());
        verify(usuarioRepository).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoExistir_AoDesativarAluno() {
        var alunoId = aluno.getId();
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        var ex = assertThrows(UsuarioNaoEncontradoException.class,
                () -> alunoService.desativarAluno(alunoId));

        assertNotNull(ex);
        assertEquals("Usuário não encontrado.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
    }

    @Test
    void deveReativarAluno_QuandoAlunoExistir() {
        var alunoId = aluno.getId();
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        alunoService.reativarAluno(alunoId);

        assertTrue(aluno.getUsuario().isAtivo());
        verify(usuarioRepository).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoExistir_AoReativarAluno() {
        var alunoId = aluno.getId();
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        var ex = assertThrows(UsuarioNaoEncontradoException.class,
                () -> alunoService.reativarAluno(alunoId));

        assertNotNull(ex);
        assertEquals("Usuário não encontrado.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
    }

    @Test
    void deveAtualizarAlunoParcialmente() {
        var request = new AlunoUpdateParcialRequest(
                "João Atualizado",
                null,
                null,
                null,
                null,
                null,
                null
        );

        var alunoExistente = aluno;
        when(alunoRepository.findById(alunoExistente.getId())).thenReturn(Optional.of(alunoExistente));

        var result = alunoService.updateAlunoParcial(alunoExistente.getId(), request);

        assertNotNull(result);
        assertEquals("João Atualizado", result.nome());
        verify(alunoRepository).findById(alunoExistente.getId());
        verify(usuarioRepository).save(alunoExistente.getUsuario());
    }

    @Test
    void deveAtualizarAlunoParcialmente_AlterandoResponsaveis() {
        var responsavelId = UUID.randomUUID();
        var request = new AlunoUpdateParcialRequest(
                null,
                null,
                null,
                null,
                null,
                List.of(responsavelId),
                null
        );

        var usuarioResponsavel = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Maria Responsável")
                .email("maria@email.com")
                .role(Role.RESPONSAVEL)
                .ativo(true)
                .build();

        var responsavel = Responsavel.builder()
                .id(responsavelId)
                .usuario(usuarioResponsavel)
                .build();

        when(alunoRepository.findById(aluno.getId())).thenReturn(Optional.of(aluno));
        when(responsavelRepository.findAllById(List.of(responsavelId))).thenReturn(List.of(responsavel));

        var result = alunoService.updateAlunoParcial(aluno.getId(), request);

        assertNotNull(result);
        verify(responsavelRepository).findAllById(List.of(responsavelId));
        verify(usuarioRepository).save(aluno.getUsuario());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoExistir_AoAtualizarAlunoParcialmente() {
        var alunoId = aluno.getId();
        var request = new AlunoUpdateParcialRequest(
                "João Atualizado",
                null,
                null,
                null,
                null,
                null,
                null
        );
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        var ex = assertThrows(UsuarioNaoEncontradoException.class,
                () -> alunoService.updateAlunoParcial(alunoId, request));

        assertNotNull(ex);
        assertEquals("Usuário não encontrado.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
    }

}