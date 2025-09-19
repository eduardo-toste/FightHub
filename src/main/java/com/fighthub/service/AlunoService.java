package com.fighthub.service;

import com.fighthub.dto.aluno.AlunoDetalhadoResponse;
import com.fighthub.dto.aluno.AlunoResponse;
import com.fighthub.dto.aluno.CriarAlunoRequest;
import com.fighthub.exception.UsuarioNaoEncontradoException;
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
        boolean menorDeIdade = Period.between(request.dataNascimento(), LocalDate.now()).getYears() < 18;

        if (menorDeIdade && (request.idsResponsaveis() == null || request.idsResponsaveis().isEmpty())) {
            throw new ValidacaoException("Aluno menor de idade deve ter ao menos um responsável");
        }

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ValidacaoException("E-mail já cadastrado");
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
                .orElseThrow(() -> new UsuarioNaoEncontradoException());

        return AlunoMapper.toDetailedDTO(aluno);
    }
}
