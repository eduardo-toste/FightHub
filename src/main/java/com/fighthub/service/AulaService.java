package com.fighthub.service;

import com.fighthub.dto.aula.AulaRequest;
import com.fighthub.dto.aula.AulaResponse;
import com.fighthub.exception.AulaNaoEncontradaException;
import com.fighthub.exception.TurmaNaoEncontradaException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.mapper.AulaMapper;
import com.fighthub.model.Aula;
import com.fighthub.model.Turma;
import com.fighthub.repository.AulaRepository;
import com.fighthub.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AulaService {

    private final AulaRepository aulaRepository;
    private final TurmaRepository turmaRepository;

    @Transactional
    public void criarAula(AulaRequest request) {
        Turma turma = null;
        if (request.turmaId() != null) turma = buscarTurmaOuLancar(request.turmaId());
        aulaRepository.save(AulaMapper.toEntity(request, turma));
    }

    public Page<AulaResponse> buscarAulas(Pageable pageable) {
        return AulaMapper.toPageDTO(aulaRepository.findAll(pageable));
    }

    public AulaResponse buscarAulaPorId(UUID idAula) {
        return AulaMapper.toDTO(buscarAulaOuLancar(idAula));
    }

    @Transactional
    public void vincularTurma(UUID idAula, UUID idTurma) {
        Aula aula = buscarAulaOuLancar(idAula);
        Turma turma = buscarTurmaOuLancar(idTurma);

        if (aula.getTurma() != null && aula.getTurma().getId().equals(turma.getId())) throw new ValidacaoException("Turma já está vinculada à aula.");

        aula.setTurma(turma);
        aulaRepository.save(aula);
    }

    @Transactional
    public void desvincularTurma(UUID idAula, UUID idTurma) {
        Aula aula = buscarAulaOuLancar(idAula);
        Turma turma = buscarTurmaOuLancar(idTurma);

        if (aula.getTurma() != turma) throw new ValidacaoException("Turma ainda não vinculada à aula.");

        aula.setTurma(null);
        aulaRepository.save(aula);
    }

    private Turma buscarTurmaOuLancar(UUID idTurma) {
        return turmaRepository.findById(idTurma)
                .orElseThrow(TurmaNaoEncontradaException::new);
    }

    private Aula buscarAulaOuLancar(UUID idAula) {
        return aulaRepository.findById(idAula)
                .orElseThrow(AulaNaoEncontradaException::new);
    }
}
