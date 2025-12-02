package com.fighthub.service;

import com.fighthub.dto.aluno.*;
import com.fighthub.exception.AlunoNaoEncontradoException;
import com.fighthub.exception.CpfExistenteException;
import com.fighthub.exception.MatriculaInvalidaException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.mapper.AlunoMapper;
import com.fighthub.model.Aluno;
import com.fighthub.model.GraduacaoAluno;
import com.fighthub.model.Responsavel;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.BeltGraduation;
import com.fighthub.model.enums.GraduationLevel;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.repository.ResponsavelRepository;
import com.fighthub.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResponsavelRepository responsavelRepository;
    private final TokenService tokenService;
    private final EmailService emailService;

    public void criarAluno(CriarAlunoRequest request) {
        var menorDeIdade = isMenorDeIdade(request.dataNascimento(), request.idsResponsaveis());

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ValidacaoException("E-mail já cadastrado");
        }

        if (usuarioRepository.findByCpf(request.cpf()).isPresent()) {
            throw new CpfExistenteException();
        }

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .cpf(request.cpf())
                .role(Role.ALUNO)
                .ativo(false)
                .loginSocial(false)
                .build());

        Aluno aluno = alunoRepository.save(Aluno.builder()
                .usuario(usuario)
                .dataMatricula(LocalDate.now())
                .dataNascimento(request.dataNascimento())
                .responsaveis(new ArrayList<>())
                .graduacao(new GraduacaoAluno(
                        BeltGraduation.BRANCA,
                        GraduationLevel.ZERO
                ))
                .build());

        if (menorDeIdade) {
            List<Responsavel> responsaveis = responsavelRepository.findAllById(request.idsResponsaveis());
            aluno.getResponsaveis().addAll(responsaveis);
            alunoRepository.save(aluno);
        }

        String token = tokenService.salvarTokenAtivacao(usuario);
        emailService.enviarEmailAtivacao(usuario, token);
    }

    public Page<AlunoResponse> obterTodos(Pageable pageable) {
        return AlunoMapper.toPage(alunoRepository.findAll(pageable));
    }

    public AlunoDetalhadoResponse obterAluno(UUID id) {
        var aluno = buscarAlunoPorId(id);

        return AlunoMapper.toDetailedDTO(aluno);
    }

    public void atualizarStatusMatricula(UUID id, AlunoUpdateMatriculaRequest request) {
        var aluno = buscarAlunoPorId(id);

        if (aluno.isMatriculaAtiva() == request.matriculaAtiva()) {
            throw new MatriculaInvalidaException();
        }

        aluno.setMatriculaAtiva(request.matriculaAtiva());
        alunoRepository.save(aluno);
    }

    public void atualizarDataNascimento(UUID id, AlunoUpdateDataNascimentoRequest request) {
        var aluno = buscarAlunoPorId(id);

        aluno.setDataNascimento(request.dataNascimento());
        alunoRepository.save(aluno);
    }

    public void atualizarDataMatricula(UUID id, AlunoUpdateDataMatriculaRequest request) {
        var aluno = buscarAlunoPorId(id);

        aluno.setDataMatricula(request.dataMatricula());
        alunoRepository.save(aluno);
    }

    @Transactional
    public void promoverFaixa(UUID idAluno) {
        var aluno = buscarAlunoPorId(idAluno);

        if (aluno.getGraduacao() == null || aluno.getGraduacao().getLevel() == null || aluno.getGraduacao().getBelt() == null)
            throw new ValidacaoException("Graduação do aluno não está inicializada");

        if (aluno.getGraduacao().getBelt() == BeltGraduation.PRETA)
            throw new ValidacaoException("Aluno já está na faixa preta.");

        if (aluno.getGraduacao().getLevel() != GraduationLevel.IV)
            throw new ValidacaoException("Não é possível promover faixa com menos de 4 graus.");

        boolean isAdult16OrOlder = Period.between(aluno.getDataNascimento(), LocalDate.now()).getYears() >= 16;

        if (aluno.getGraduacao().getBelt() == BeltGraduation.BRANCA && isAdult16OrOlder) {
            aluno.getGraduacao().setBelt(BeltGraduation.AZUL);
            aluno.getGraduacao().setLevel(GraduationLevel.ZERO);
            alunoRepository.save(aluno);
            return;
        }

        aluno.getGraduacao().promoteBelt();
        aluno.getGraduacao().setLevel(GraduationLevel.ZERO);
        alunoRepository.save(aluno);
    }

    @Transactional
    public void rebaixarFaixa(UUID idAluno) {
        var aluno = buscarAlunoPorId(idAluno);

        if (aluno.getGraduacao() == null || aluno.getGraduacao().getLevel() == null || aluno.getGraduacao().getBelt() == null) {
            throw new ValidacaoException("Graduação do aluno não está inicializada");
        }

        if (aluno.getGraduacao().getLevel() != GraduationLevel.ZERO)
            throw new ValidacaoException("Não é possível rebaixar faixa com mais de zero graus.");

        if (aluno.getGraduacao().getBelt() == BeltGraduation.BRANCA)
            throw new ValidacaoException("Aluno já está na faixa branca.");

        boolean isAdult16OrOlder = Period.between(aluno.getDataNascimento(), LocalDate.now()).getYears() >= 16;

        if (aluno.getGraduacao().getBelt() == BeltGraduation.AZUL && isAdult16OrOlder) {
            aluno.getGraduacao().setBelt(BeltGraduation.BRANCA);
            aluno.getGraduacao().setLevel(GraduationLevel.IV);
            alunoRepository.save(aluno);
            return;
        }

        aluno.getGraduacao().demoteBelt();
        aluno.getGraduacao().setLevel(GraduationLevel.IV);
        alunoRepository.save(aluno);
    }

    @Transactional
    public void promoverGrau(UUID id) {
        var aluno = buscarAlunoPorId(id);

        if (aluno.getGraduacao() == null || aluno.getGraduacao().getLevel() == null) {
            throw new ValidacaoException("Graduação do aluno não está inicializada");
        }

        if (aluno.getGraduacao().getLevel() == GraduationLevel.IV) {
            throw new ValidacaoException("Aluno já está no grau máximo.");
        }

        aluno.getGraduacao().promoteLevel();
        alunoRepository.save(aluno);
    }

    @Transactional
    public void rebaixarGrau(UUID id) {
        var aluno = buscarAlunoPorId(id);

        if (aluno.getGraduacao() == null || aluno.getGraduacao().getLevel() == null) {
            throw new ValidacaoException("Graduação do aluno não está inicializada");
        }

        if (aluno.getGraduacao().getLevel() == GraduationLevel.ZERO) {
            throw new ValidacaoException("Aluno já está no grau mínimo.");
        }

        aluno.getGraduacao().demoteLevel();
        alunoRepository.save(aluno);
    }
    
    private Aluno buscarAlunoPorId(UUID id) {
        return alunoRepository.findById(id)
                .orElseThrow(AlunoNaoEncontradoException::new);
    }

    private boolean isMenorDeIdade(LocalDate dataNascimento, List<UUID> idsResponsaveis) {
        boolean menorDeIdade = Period.between(dataNascimento, LocalDate.now()).getYears() < 18;

        if (menorDeIdade && (idsResponsaveis == null || idsResponsaveis.isEmpty())) {
            throw new ValidacaoException("Aluno menor de idade deve ter ao menos um responsável");
        }

        return menorDeIdade;
    }
}
