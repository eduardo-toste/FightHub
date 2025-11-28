package com.fighthub.service;

import com.fighthub.dto.aula.AulaRequest;
import com.fighthub.dto.aula.AulaResponse;
import com.fighthub.dto.aula.AulaUpdateCompletoRequest;
import com.fighthub.dto.aula.AulaUpdateStatusRequest;
import com.fighthub.exception.*;
import com.fighthub.mapper.AulaMapper;
import com.fighthub.model.Aluno;
import com.fighthub.model.Aula;
import com.fighthub.model.Turma;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.ClassStatus;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.repository.AulaRepository;
import com.fighthub.repository.TurmaRepository;
import com.fighthub.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AulaService {

    private final AulaRepository aulaRepository;
    private final TurmaRepository turmaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final JwtService jwtService;

    @Transactional
    public void criarAula(AulaRequest request) {
        Turma turma = null;
        if (request.turmaId() != null) turma = buscarTurmaOuLancar(request.turmaId());
        aulaRepository.save(AulaMapper.toEntity(request, turma));
    }

    public Page<AulaResponse> buscarAulas(Pageable pageable) {
        return AulaMapper.toPageDTO(aulaRepository.findAll(pageable));
    }

    public Page<AulaResponse> buscarAulasDisponiveisAluno(Pageable pageable, HttpServletRequest request) {
        List<Turma> turmasMatriculadas = buscarTurmasMatriculadasPorAluno(request);

        return aulaRepository.findByStatusAndTurmaIn(ClassStatus.DISPONIVEL ,turmasMatriculadas, pageable)
                .map(AulaMapper::toDTO);
    }

    public AulaResponse buscarAulaPorId(UUID idAula) {
        return AulaMapper.toDTO(buscarAulaOuLancar(idAula));
    }

    @Transactional
    public AulaResponse atualizarAula(AulaUpdateCompletoRequest request, UUID id) {
        Aula aula = buscarAulaOuLancar(id);
        Turma turma = buscarTurmaOuLancar(request.turmaId());
        aula.putUpdate(request, turma);
        return AulaMapper.toDTO(aulaRepository.save(aula));
    }

    @Transactional
    public AulaResponse atualizarStatus(UUID id, AulaUpdateStatusRequest request) {
        Aula aula = buscarAulaOuLancar(id);
        aula.setStatus(request.status());
        return AulaMapper.toDTO(aulaRepository.save(aula));
    }

    @Transactional
    public void vincularTurma(UUID idAula, UUID idTurma) {
        Aula aula = buscarAulaOuLancar(idAula);
        Turma turma = buscarTurmaOuLancar(idTurma);

        if (aula.getTurma() != null && aula.getTurma().getId().equals(turma.getId()))
            throw new ValidacaoException("Turma já está vinculada à aula.");

        aula.setTurma(turma);
        aulaRepository.save(aula);
    }

    @Transactional
    public void desvincularTurma(UUID idAula, UUID idTurma) {
        Aula aula = buscarAulaOuLancar(idAula);
        Turma turma = buscarTurmaOuLancar(idTurma);

        if (aula.getTurma() == null || !aula.getTurma().getId().equals(turma.getId()))
            throw new ValidacaoException("Turma ainda não vinculada à aula.");

        aula.setTurma(null);
        aulaRepository.save(aula);
    }

    public void excluirAula(UUID id) {
        buscarAulaOuLancar(id);
        aulaRepository.deleteById(id);
    }

    private Turma buscarTurmaOuLancar(UUID idTurma) {
        return turmaRepository.findById(idTurma)
                .orElseThrow(TurmaNaoEncontradaException::new);
    }

    private Aula buscarAulaOuLancar(UUID idAula) {
        return aulaRepository.findById(idAula)
                .orElseThrow(AulaNaoEncontradaException::new);
    }

    private List<Turma> buscarTurmasMatriculadasPorAluno(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String email = jwtService.extrairEmail(authHeader.substring(7));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        Aluno aluno = alunoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(AlunoNaoEncontradoException::new);

        return turmaRepository.findAllByAlunos(aluno);
    }
}