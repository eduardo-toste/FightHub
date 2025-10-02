package com.fighthub.service;

import com.fighthub.dto.aluno.*;
import com.fighthub.exception.AlunoNaoEncontradoException;
import com.fighthub.exception.CpfExistenteException;
import com.fighthub.exception.MatriculaInvalidaException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.mapper.AlunoMapper;
import com.fighthub.model.Aluno;
import com.fighthub.model.Responsavel;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.repository.ResponsavelRepository;
import com.fighthub.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

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
        var aluno = alunoRepository.findById(id)
                .orElseThrow(AlunoNaoEncontradoException::new);

        return AlunoMapper.toDetailedDTO(aluno);
    }

    public void atualizarStatusMatricula(UUID id, AlunoUpdateMatriculaRequest request) {
        var aluno = alunoRepository.findById(id)
                .orElseThrow(AlunoNaoEncontradoException::new);

        if (aluno.isMatriculaAtiva() == request.matriculaAtiva()) {
            throw new MatriculaInvalidaException();
        }

        aluno.setMatriculaAtiva(request.matriculaAtiva());
        alunoRepository.save(aluno);
    }

    private boolean isMenorDeIdade(LocalDate dataNascimento, List<UUID> idsResponsaveis) {
        boolean menorDeIdade = Period.between(dataNascimento, LocalDate.now()).getYears() < 18;

        if (menorDeIdade && (idsResponsaveis == null || idsResponsaveis.isEmpty())) {
            throw new ValidacaoException("Aluno menor de idade deve ter ao menos um responsável");
        }

        return menorDeIdade;
    }
}
