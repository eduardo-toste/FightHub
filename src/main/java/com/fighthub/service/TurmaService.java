package com.fighthub.service;

import com.fighthub.dto.turma.TurmaRequest;
import com.fighthub.dto.turma.TurmaResponse;
import com.fighthub.exception.ProfessorNaoEncontradoException;
import com.fighthub.exception.TurmaNaoEncontradaException;
import com.fighthub.mapper.TurmaMapper;
import com.fighthub.model.Professor;
import com.fighthub.repository.ProfessorRepository;
import com.fighthub.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final ProfessorRepository professorRepository;

    public void criarTurma(TurmaRequest request) {
        Professor professor = professorRepository.findById(request.professorId())
                        .orElseThrow(ProfessorNaoEncontradoException::new);

        turmaRepository.save(TurmaMapper.toEntity(request, professor));
    }

    public Page<TurmaResponse> buscarTurmas(Pageable pageable) {
        return TurmaMapper.toPageDTO(turmaRepository.findAll(pageable));
    }

    public TurmaResponse buscarTurmaPorId(UUID id) {
        return TurmaMapper.toDTO(turmaRepository.findById(id)
                .orElseThrow(TurmaNaoEncontradaException::new));
    }
}
