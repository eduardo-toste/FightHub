package com.fighthub.service;

import com.fighthub.dto.presenca.PresencaRequest;
import com.fighthub.exception.*;
import com.fighthub.model.*;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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

    private Usuario obterUsuarioLogado(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String email = jwtService.extrairEmail(authHeader.substring(7));
        return usuarioRepository.findByEmail(email)
                .orElseThrow(UsuarioNaoEncontradoException::new);
    }
}
