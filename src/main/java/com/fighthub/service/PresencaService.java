package com.fighthub.service;

import com.fighthub.dto.presenca.PresencaRequest;
import com.fighthub.dto.presenca.PresencaResponse;
import com.fighthub.exception.*;
import com.fighthub.mapper.PresencaMapper;
import com.fighthub.model.*;
import com.fighthub.model.enums.Role;
import com.fighthub.model.enums.SubscriptionStatus;
import com.fighthub.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresencaService {

    private final PresencaRepository presencaRepository;
    private final AulaRepository aulaRepository;
    private final InscricaoRepository inscricaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfessorRepository professorRepository;
    private final TurmaRepository turmaRepository;
    private final AlunoRepository alunoRepository;
    private final JwtService jwtService;

    @Transactional
    public void atualizarStatusPresencaPorInscricao(UUID idAula, 
                                  UUID idInscricao,
                                  PresencaRequest request, 
                                  HttpServletRequest httpServletRequest) {
        
        Usuario usuarioLogado = obterUsuarioLogado(httpServletRequest);
        Inscricao inscricao = buscarInscricaoPorId(idInscricao);
        Optional<Presenca> presenca = validarPresencaOperacao(idAula, inscricao, usuarioLogado);

        presenca.ifPresentOrElse(
                presencaExistente -> {
                    if (presencaExistente.isPresente() == request.presente()) {
                        throw new ValidacaoException("Presença já registrada com o mesmo status.");
                    }
                    presencaExistente.setPresente(request.presente());
                    presencaRepository.save(presencaExistente);
                },
                () -> {
                    Presenca novaPresenca = Presenca.builder()
                            .inscricao(inscricao)
                            .presente(request.presente())
                            .dataRegistro(LocalDate.now())
                            .build();
                    presencaRepository.save(novaPresenca);
                }
        );
    }

    @Transactional(readOnly = true)
    public Page<PresencaResponse> listarPresencasPorAula(UUID idAula, Pageable pageable, HttpServletRequest httpServletRequest) {
        Usuario usuarioLogado = obterUsuarioLogado(httpServletRequest);
        Aula aula = buscarAulaPorId(idAula);

        if (usuarioLogado.getRole() == Role.PROFESSOR && !verificarSeProfessorDaAula(usuarioLogado, aula)) {
            throw new ValidacaoException("Professor não autorizado a verificar presenças para esta aula.");
        }

        List<Inscricao> inscricoes = buscarInscricoesPorAulaEStatus(aula, SubscriptionStatus.INSCRITO);
        return PresencaMapper.toPageDTO(presencaRepository.findAllByInscricaoIn(inscricoes, pageable));
    }

    @Transactional(readOnly = true)
    public Page<PresencaResponse> listarMinhasPresencas(Pageable pageable, HttpServletRequest httpServletRequest) {
        Usuario usuarioLogado = obterUsuarioLogado(httpServletRequest);

        if (usuarioLogado.getRole() != Role.ALUNO) {
            throw new ValidacaoException("Apenas alunos podem acessar suas presenças.");
        }

        Aluno aluno = buscarAlunoPorUsuario(usuarioLogado);

        Page<Inscricao> inscricoesDoAluno = inscricaoRepository.findAllByAlunoAndStatus(
                aluno,
                SubscriptionStatus.INSCRITO,
                pageable
        );

        List<Inscricao> inscricoesList = inscricoesDoAluno.getContent();

        if (inscricoesList.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Presenca> presencas = presencaRepository.findAllByInscricaoIn(inscricoesList, pageable);
        return PresencaMapper.toPageDTO(presencas);
    }

    private Optional<Presenca> validarPresencaOperacao(UUID idAula, Inscricao inscricao, Usuario usuarioLogado) {
        Aula aula = buscarAulaPorId(idAula);
        Role role = usuarioLogado.getRole();

        if (role == Role.PROFESSOR && !verificarSeProfessorDaAula(usuarioLogado, aula)) {
            throw new ValidacaoException("Professor não autorizado a registrar/cancelar presença para esta aula.");
        }
        if (!inscricao.getAula().equals(aula)) {
            throw new ValidacaoException("Inscrição não pertence a esta aula.");
        }
        return buscarPresencaPorInscricao(inscricao);
    }

    private boolean verificarSeProfessorDaAula(Usuario usuario, Aula aula) {
        Professor professor = buscarProfessorPorUsuario(usuario);
        List<Turma> turmasDoProfessor = buscarTurmasDoProfessor(professor);
        return turmasDoProfessor.contains(aula.getTurma());
    }

    private Aluno buscarAlunoPorUsuario(Usuario usuario) {
        return alunoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(AlunoNaoEncontradoException::new);
    }

    private Optional<Presenca> buscarPresencaPorInscricao(Inscricao inscricao) {
        return presencaRepository.findByInscricao(inscricao);
    }

    private List<Turma> buscarTurmasDoProfessor(Professor professor) {
        return turmaRepository.findAllByProfessor(professor);
    }

    private Professor buscarProfessorPorUsuario(Usuario usuario) {
        return professorRepository.findByUsuario(usuario)
                .orElseThrow(ProfessorNaoEncontradoException::new);
    }

    private Aula buscarAulaPorId(UUID idAula) {
        return aulaRepository.findById(idAula)
                .orElseThrow(AulaNaoEncontradaException::new);
    }

    private Inscricao buscarInscricaoPorId(UUID idInscricao) {
        return inscricaoRepository.findById(idInscricao)
                .orElseThrow(InscricaoNaoEncontradaException::new);
    }

    private List<Inscricao> buscarInscricoesPorAulaEStatus(Aula aula, SubscriptionStatus status) {
        return inscricaoRepository.findAllByAulaAndStatus(aula, status);
    }

    private Usuario obterUsuarioLogado(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String email = jwtService.extrairEmail(authHeader.substring(7));
        return usuarioRepository.findByEmail(email)
                .orElseThrow(UsuarioNaoEncontradoException::new);
    }
}
