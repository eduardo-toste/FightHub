package com.fighthub.service;

import com.fighthub.dto.inscricao.InscricaoResponse;
import com.fighthub.exception.AlunoNaoEncontradoException;
import com.fighthub.exception.AulaNaoEncontradaException;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.mapper.InscricaoMapper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InscricaoService {

    private final InscricaoRepository inscricaoRepository;
    private final AlunoRepository alunoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AulaRepository aulaRepository;
    private final JwtService jwtService;

    @Transactional
    public void inscreverAluno(UUID idAula, HttpServletRequest request) {
        Aula aula = buscarAulaPorId(idAula);
        Aluno aluno = obterAlunoLogado(request);

        var optional = inscricaoRepository.findByAulaAndAluno(aula, aluno);
        if (optional.isPresent()) {
            Inscricao inscricao = optional.get();

            if (inscricao.getStatus() == SubscriptionStatus.INSCRITO) {
                throw new ValidacaoException("Aluno já inscrito na aula.");
            }

            inscricao.setStatus(SubscriptionStatus.INSCRITO);
            inscricao.setInscritoEm(LocalDateTime.now());
            inscricaoRepository.save(inscricao);
            return;
        }

        inscricaoRepository.save(new Inscricao(aluno, aula, SubscriptionStatus.INSCRITO, LocalDateTime.now()));
    }

    @Transactional
    public void cancelarInscricao(UUID idAula, HttpServletRequest request) {
        Aula aula = buscarAulaPorId(idAula);
        Aluno aluno = obterAlunoLogado(request);

        Inscricao inscricao = inscricaoRepository.findByAulaAndAluno(aula, aluno)
                .orElseThrow(() -> new ValidacaoException("Aluno não está inscrito na aula."));

        if (inscricao.getStatus().equals(SubscriptionStatus.CANCELADO)) {
            throw new ValidacaoException("Inscrição já está cancelada.");
        }

        inscricao.setStatus(SubscriptionStatus.CANCELADO);
        inscricaoRepository.save(inscricao);
    }

    public Page<InscricaoResponse> buscarInscricoesPorAula(UUID idAula, Pageable pageable) {
        Aula aula = buscarAulaPorId(idAula);
        return InscricaoMapper.toPageDTO(inscricaoRepository.findAllByAula(aula, pageable));
    }

    public Page<InscricaoResponse> buscarInscricoesProprias(HttpServletRequest request, Pageable pageable) {
        Aluno aluno = obterAlunoLogado(request);
        return InscricaoMapper.toPageDTO(inscricaoRepository.findAllByAlunoAndStatus(aluno, SubscriptionStatus.INSCRITO, pageable));
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
}
