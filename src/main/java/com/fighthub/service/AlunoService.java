package com.fighthub.service;

import com.fighthub.dto.aluno.*;
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
                .orElseThrow(UsuarioNaoEncontradoException::new);

        return AlunoMapper.toDetailedDTO(aluno);
    }

    public AlunoDetalhadoResponse updateAlunoCompleto(UUID id, AlunoUpdateCompletoRequest request) {
        var aluno = alunoRepository.findById(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        isMenorDeIdade(request.dataNascimento(), request.idsResponsaveis());

        var alunoAtualizado = atualizacaoCompleta(aluno, request);
        return AlunoMapper.toDetailedDTO(alunoAtualizado);
    }

    public AlunoDetalhadoResponse updateAlunoParcial(UUID id, AlunoUpdateParcialRequest request) {
        var aluno = alunoRepository.findById(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        var alunoAtualizado = atualizacaoParcial(aluno, request);
        return AlunoMapper.toDetailedDTO(alunoAtualizado);
    }

    public void desativarAluno(UUID id) {
        alterarStatusAluno(id, false);
    }

    public void reativarAluno(UUID id) {
        alterarStatusAluno(id, true);
    }

    private void alterarStatusAluno(UUID id, boolean status) {
        Aluno aluno = alunoRepository.findById(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        aluno.getUsuario().setAtivo(status);
        usuarioRepository.save(aluno.getUsuario());
    }

    private Aluno atualizacaoParcial(Aluno aluno, AlunoUpdateParcialRequest request) {
        List<Responsavel> responsaveis = request.idsResponsaveis() != null
                ? responsavelRepository.findAllById(request.idsResponsaveis())
                : List.of();

        aluno.patchUpdate(request, responsaveis);
        usuarioRepository.save(aluno.getUsuario());
        return aluno;
    }

    private Aluno atualizacaoCompleta(Aluno aluno, AlunoUpdateCompletoRequest request) {
        List<Responsavel> responsaveis = request.idsResponsaveis() != null
                ? responsavelRepository.findAllById(request.idsResponsaveis())
                : List.of();

        aluno.putUpdate(request, responsaveis);
        usuarioRepository.save(aluno.getUsuario());
        return aluno;
    }

    private boolean isMenorDeIdade(LocalDate dataNascimento, List<UUID> idsResponsaveis) {
        boolean menorDeIdade = Period.between(dataNascimento, LocalDate.now()).getYears() < 18;

        if (menorDeIdade && (idsResponsaveis == null || idsResponsaveis.isEmpty())) {
            throw new ValidacaoException("Aluno menor de idade deve ter ao menos um responsável");
        }

        return menorDeIdade;
    }
}
