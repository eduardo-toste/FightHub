package com.fighthub.service;

import com.fighthub.dto.responsavel.CriarResponsavelRequest;
import com.fighthub.dto.responsavel.ResponsavelDetalhadoResponse;
import com.fighthub.dto.responsavel.ResponsavelResponse;
import com.fighthub.dto.inscricao.InscricaoResponse;
import com.fighthub.exception.AlunoNaoEncontradoException;
import com.fighthub.exception.CpfExistenteException;
import com.fighthub.exception.ResponsavelNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.mapper.ResponsavelMapper;
import com.fighthub.mapper.InscricaoMapper;
import com.fighthub.model.Responsavel;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.repository.ResponsavelRepository;
import com.fighthub.repository.UsuarioRepository;
import com.fighthub.repository.InscricaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResponsavelService {

    private final ResponsavelRepository responsavelRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final InscricaoRepository inscricaoRepository;
    private final TokenService tokenService;
    private final EmailService emailService;

    @Transactional
    public void criacaoResponsavel(CriarResponsavelRequest request) {
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
                .role(Role.RESPONSAVEL)
                .ativo(false)
                .loginSocial(false)
                .build());

        responsavelRepository.save(Responsavel.builder()
                .usuario(usuario)
                .build());

        String token = tokenService.salvarTokenAtivacao(usuario);
        emailService.enviarEmailAtivacao(usuario, token);
    }

    public Page<ResponsavelResponse> obterTodosResponsaveis(Pageable pageable) {
        return ResponsavelMapper.toPageDTO(responsavelRepository.findAll(pageable));
    }

    public ResponsavelDetalhadoResponse obterResponsavelPorId(UUID id) {
        return ResponsavelMapper.toDetailedDTO(responsavelRepository.findById(id)
                .orElseThrow(ResponsavelNaoEncontradoException::new));
    }

    @Transactional
    public void vincularAlunoAoResponsavel(UUID idResponsavel, UUID idAluno) {
        var responsavel = responsavelRepository.findById(idResponsavel)
                .orElseThrow(ResponsavelNaoEncontradoException::new);

        var aluno = alunoRepository.findById(idAluno)
                .orElseThrow(AlunoNaoEncontradoException::new);

        if (responsavel.getAlunos().contains(aluno)) throw new ValidacaoException("Vínculo de responsabilidade já estabelecido.");

        responsavel.getAlunos().add(aluno);
        responsavelRepository.save(responsavel);
    }

    @Transactional
    public void removerVinculoAlunoEResponsavel(UUID idResponsavel, UUID idAluno) {
        var responsavel = responsavelRepository.findById(idResponsavel)
                .orElseThrow(ResponsavelNaoEncontradoException::new);

        var aluno = alunoRepository.findById(idAluno)
                .orElseThrow(AlunoNaoEncontradoException::new);

        if (!responsavel.getAlunos().contains(aluno)) throw new ValidacaoException("Responsável não vinculado ao aluno.");

        responsavel.getAlunos().remove(aluno);
        responsavelRepository.save(responsavel);
    }

    @Transactional(readOnly = true)
    public ResponsavelDetalhadoResponse obterDadosPropriosResponsavel() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ValidacaoException("Usuário não autenticado");
        }

        String email = authentication.getName();
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ValidacaoException("Usuário não encontrado"));

        if (usuario.getRole() != Role.RESPONSAVEL) {
            throw new ValidacaoException("Apenas responsáveis podem acessar este endpoint");
        }

        var responsavel = responsavelRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(ResponsavelNaoEncontradoException::new);

        return ResponsavelMapper.toDetailedDTO(responsavel);
    }

    @Transactional(readOnly = true)
    public List<InscricaoResponse> obterInscricoesDependentes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ValidacaoException("Usuário não autenticado");
        }

        String email = authentication.getName();
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ValidacaoException("Usuário não encontrado"));

        if (usuario.getRole() != Role.RESPONSAVEL) {
            throw new ValidacaoException("Apenas responsáveis podem acessar este endpoint");
        }

        var responsavel = responsavelRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(ResponsavelNaoEncontradoException::new);

        var alunosIds = responsavel.getAlunos().stream()
                .map(aluno -> aluno.getId())
                .toList();

        if (alunosIds.isEmpty()) {
            return List.of();
        }

        var inscricoes = inscricaoRepository.findByAlunoIdIn(alunosIds);
        return InscricaoMapper.toListDTO(inscricoes);
    }
}
