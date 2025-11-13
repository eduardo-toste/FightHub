package com.fighthub.service;

import com.fighthub.exception.*;
import com.fighthub.model.Aluno;
import com.fighthub.model.Aula;
import com.fighthub.model.Inscricao;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.SubscriptionStatus;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.repository.AulaRepository;
import com.fighthub.repository.InscricaoRepository;
import com.fighthub.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InscricaoService {

    private final InscricaoRepository inscricaoRepository;
    private final AlunoRepository alunoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AulaRepository aulaRepository;
    private final JwtService jwtService;

    public void inscricaoAluno(UUID idAula, HttpServletRequest request) {
        Aula aula = buscarAulaPorId(idAula);
        Aluno aluno = obterAlunoLogado(request);
        if (inscricaoRepository.findByAulaAndAluno(aula, aluno).isPresent()) throw new ValidacaoException("Aluno já inscrito na aula.");
        inscricaoRepository.save(new Inscricao(aluno, aula, SubscriptionStatus.INSCRITO, LocalDate.now()));
    }

    public void desinscricaoAluno(UUID idAula, HttpServletRequest request) {
        Aula aula = buscarAulaPorId(idAula);
        Aluno aluno = obterAlunoLogado(request);
        Inscricao inscricao = buscarInscricaoPorAulaEAluno(aula, aluno);
        inscricao.setStatus(SubscriptionStatus.CANCELADO);
        inscricaoRepository.save(inscricao);
    }

    private Aluno obterAlunoLogado(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String email = jwtService.extrairEmail(authHeader.substring(7));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        return alunoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(AlunoNaoEncontradoException::new);
    }

    private Aula buscarAulaPorId(UUID idAula) {
        return aulaRepository.findById(idAula)
                .orElseThrow(AulaNaoEncontradaException::new);
    }

    private Inscricao buscarInscricaoPorAulaEAluno(Aula aula, Aluno aluno) {
        return inscricaoRepository.findByAulaAndAluno(aula, aluno)
                .orElseThrow(InscricaoNaoEncontradaException::new);
    }
}
