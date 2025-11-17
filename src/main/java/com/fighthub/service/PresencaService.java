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
    public void registrarPresenca(UUID idAula, PresencaRequest request, HttpServletRequest httpServletRequest) {
        Aula aula = buscarAulaPorId(idAula);
        Inscricao inscricao = buscarInscricaoPorId(request.inscricaoId());
        Usuario usuarioLogado = obterUsuarioLogado(httpServletRequest);
        Role role = usuarioLogado.getRole();

        if (role != Role.ADMIN && role != Role.PROFESSOR) {
            throw new ValidacaoException("Professor não autorizado a registrar presença para esta aula.");
        }

        if (role == Role.PROFESSOR && !verificarSeProfessorDaAula(usuarioLogado, aula)) {
            throw new ValidacaoException("Professor não autorizado a registrar presença para esta aula.");
        }

        if (!inscricao.getAula().equals(aula)) throw new ValidacaoException("Inscrição não pertence a esta aula.");

        Optional<Presenca> presenca = buscarPresencaPorInscricao(inscricao);
        if (presenca.isPresent()) {
            Presenca presencaExistente = presenca.get();
            presencaExistente.setPresente(request.presente());
            presencaRepository.save(presencaExistente);
        } else {
            Presenca novaPresenca = Presenca.builder()
                    .inscricao(inscricao)
                    .presente(request.presente())
                    .dataRegistro(LocalDate.now())
                    .build();

            presencaRepository.save(novaPresenca);
        }
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
