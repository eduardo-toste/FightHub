package com.fighthub.service;

import com.fighthub.dto.aluno.*;
import com.fighthub.exception.AlunoNaoEncontradoException;
import com.fighthub.exception.CpfExistenteException;
import com.fighthub.exception.MatriculaInvalidaException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.model.*;
import com.fighthub.model.enums.BeltGraduation;
import com.fighthub.model.enums.GraduationLevel;
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
                .graduacao(new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                ))
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

    @Test
    void devePromoverFaixaAlunoComSucesso_QuantoAlunoTiverMaisDe16Anos() {
        var alunoId = aluno.getId();
        aluno.getGraduacao().setLevel(GraduationLevel.IV);
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        alunoService.promoverFaixa(alunoId);

        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository).save(argThat(a -> a.getGraduacao().getBelt() == BeltGraduation.AZUL));
    }

    @Test
    void devePromoverFaixaAlunoComSucesso_QuantoAlunoTiverMenosDe16Anos() {
        var alunoId = aluno.getId();
        aluno.getGraduacao().setLevel(GraduationLevel.IV);
        aluno.setDataNascimento(LocalDate.now().minusYears(15));
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        alunoService.promoverFaixa(alunoId);

        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository).save(argThat(a -> a.getGraduacao().getBelt() == BeltGraduation.CINZA));
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoExistir_AoPromoverFaixa() {
        var alunoId = aluno.getId();
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        var ex = assertThrows(AlunoNaoEncontradoException.class,
                () -> alunoService.promoverFaixa(alunoId));

        assertNotNull(ex);
        assertEquals("Aluno não encontrado.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoGraduacaoNaoEstiverInicializada_AoPromoverFaixa() {
        var alunoId = aluno.getId();
        aluno.setGraduacao(null);
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(ValidacaoException.class,
                () -> alunoService.promoverFaixa(alunoId));

        assertNotNull(ex);
        assertEquals("Graduação do aluno não está inicializada", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoPromoverFaixaAlunoComMenosDe4Graus() {
        var alunoId = aluno.getId();
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(ValidacaoException.class,
                () -> alunoService.promoverFaixa(alunoId));

        assertNotNull(ex);
        assertEquals("Não é possível promover faixa com menos de 4 graus.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoEstiverNaFaixaPreta_AoPromoverFaixa() {
        var alunoId = aluno.getId();
        aluno.getGraduacao().setBelt(BeltGraduation.PRETA);
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(ValidacaoException.class,
                () -> alunoService.promoverFaixa(alunoId));

        assertNotNull(ex);
        assertEquals("Aluno já está na faixa preta.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveRebaixarFaixaAlunoComSucesso_QuantoAlunoTiverMaisDe16Anos() {
        var alunoId = aluno.getId();
        aluno.getGraduacao().setBelt(BeltGraduation.AZUL);
        aluno.getGraduacao().setLevel(GraduationLevel.ZERO);
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        alunoService.rebaixarFaixa(alunoId);

        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository).save(argThat(a -> a.getGraduacao().getBelt() == BeltGraduation.BRANCA));
    }

    @Test
    void deveRebaixarFaixaAlunoComSucesso_QuantoAlunoTiverMenosDe16Anos() {
        var alunoId = aluno.getId();
        aluno.getGraduacao().setBelt(BeltGraduation.CINZA);
        aluno.getGraduacao().setLevel(GraduationLevel.ZERO);
        aluno.setDataNascimento(LocalDate.now().minusYears(15));
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        alunoService.rebaixarFaixa(alunoId);

        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository).save(argThat(a -> a.getGraduacao().getBelt() == BeltGraduation.BRANCA));
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoExistir_AoRebaixarFaixa() {
        var alunoId = aluno.getId();
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        var ex = assertThrows(AlunoNaoEncontradoException.class,
                () -> alunoService.rebaixarFaixa(alunoId));

        assertNotNull(ex);
        assertEquals("Aluno não encontrado.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoGraduacaoNaoEstiverInicializada_AoRebaixarFaixa() {
        var alunoId = aluno.getId();
        aluno.setGraduacao(null);
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(ValidacaoException.class,
                () -> alunoService.rebaixarFaixa(alunoId));

        assertNotNull(ex);
        assertEquals("Graduação do aluno não está inicializada", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoRebaixarFaixaAlunoComMaisDe0Graus() {
        var alunoId = aluno.getId();
        aluno.getGraduacao().setLevel(GraduationLevel.IV);
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(ValidacaoException.class,
                () -> alunoService.rebaixarFaixa(alunoId));

        assertNotNull(ex);
        assertEquals("Não é possível rebaixar faixa com mais de zero graus.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoRebaixarFaixaAlunoNaFaixaBranca_AoRebaixarFaixa() {
        var alunoId = aluno.getId();
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(ValidacaoException.class,
                () -> alunoService.rebaixarFaixa(alunoId));

        assertNotNull(ex);
        assertEquals("Aluno já está na faixa branca.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void devePromoverGrauAlunoComSucesso() {
        var alunoId = aluno.getId();
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        alunoService.promoverGrau(alunoId);

        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository).save(argThat(a -> a.getGraduacao().getLevel() == GraduationLevel.I));
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoExistir_AoPromoverGrau() {
        var alunoId = aluno.getId();
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        var ex = assertThrows(AlunoNaoEncontradoException.class,
                () -> alunoService.promoverGrau(alunoId));

        assertNotNull(ex);
        assertEquals("Aluno não encontrado.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoGraduacaoNaoEstiverInicializada_AoPromoverGrau() {
        var alunoId = aluno.getId();
        aluno.setGraduacao(null);
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(ValidacaoException.class,
                () -> alunoService.promoverGrau(alunoId));

        assertNotNull(ex);
        assertEquals("Graduação do aluno não está inicializada", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNoGrauMaximo_AoPromoverGrau() {
        var alunoId = aluno.getId();
        aluno.getGraduacao().setLevel(GraduationLevel.IV);
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(ValidacaoException.class,
                () -> alunoService.promoverGrau(alunoId));

        assertNotNull(ex);
        assertEquals("Aluno já está no grau máximo.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveRebaixarGrauAlunoComSucesso() {
        var alunoId = aluno.getId();
        aluno.getGraduacao().setLevel(GraduationLevel.II);
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        alunoService.rebaixarGrau(alunoId);

        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository).save(argThat(a -> a.getGraduacao().getLevel() == GraduationLevel.I));
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNaoExistir_AoRebaixarGrau() {
        var alunoId = aluno.getId();
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.empty());

        var ex = assertThrows(AlunoNaoEncontradoException.class,
                () -> alunoService.rebaixarGrau(alunoId));

        assertNotNull(ex);
        assertEquals("Aluno não encontrado.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoGraduacaoNaoEstiverInicializada_AoRebaixarGrau() {
        var alunoId = aluno.getId();
        aluno.setGraduacao(null);
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(ValidacaoException.class,
                () -> alunoService.rebaixarGrau(alunoId));

        assertNotNull(ex);
        assertEquals("Graduação do aluno não está inicializada", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoAlunoNoGrauMinimo_AoRebaixarGrau() {
        var alunoId = aluno.getId();
        when(alunoRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        var ex = assertThrows(ValidacaoException.class,
                () -> alunoService.rebaixarGrau(alunoId));

        assertNotNull(ex);
        assertEquals("Aluno já está no grau mínimo.", ex.getMessage());
        verify(alunoRepository).findById(alunoId);
        verify(alunoRepository, never()).save(any());
    }
}