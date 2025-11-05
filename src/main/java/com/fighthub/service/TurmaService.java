package com.fighthub.service;

import com.fighthub.dto.turma.TurmaRequest;
import com.fighthub.dto.turma.TurmaResponse;
import com.fighthub.dto.turma.TurmaUpdateCompletoRequest;
import com.fighthub.dto.turma.TurmaUpdateStatusRequest;
import com.fighthub.exception.*;
import com.fighthub.mapper.TurmaMapper;
import com.fighthub.model.Aluno;
import com.fighthub.model.Professor;
import com.fighthub.model.Turma;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.repository.ProfessorRepository;
import com.fighthub.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;

    @Transactional
    public void criarTurma(TurmaRequest request) {
        Professor professor = null;

        if (request.professorId() != null) {
            professor = buscarProfessorOuLancar(request.professorId());
        }

        turmaRepository.save(TurmaMapper.toEntity(request, professor));
    }

    public Page<TurmaResponse> buscarTurmas(Pageable pageable) {
        return TurmaMapper.toPageDTO(turmaRepository.findAll(pageable));
    }

    public TurmaResponse buscarTurmaPorId(UUID id) {
        Turma turma = buscarTurmaOuLancar(id);
        return TurmaMapper.toDTO(turma);
    }

    @Transactional
    public TurmaResponse atualizarTurma(UUID id, TurmaUpdateCompletoRequest request) {
        Turma turma = buscarTurmaOuLancar(id);
        Professor professor = buscarProfessorOuLancar(request.professorId());
        turma.putUpdate(request, professor);
        return TurmaMapper.toDTO(turmaRepository.save(turma));
    }

    @Transactional
    public TurmaResponse atualizarStatusTurma(UUID id, TurmaUpdateStatusRequest request) {
        Turma turma = buscarTurmaOuLancar(id);
        turma.setAtivo(request.ativo());
        return TurmaMapper.toDTO(turmaRepository.save(turma));
    }

    @Transactional
    public void excluirTurma(UUID id) {
        Turma turma = buscarTurmaOuLancar(id);
        turmaRepository.delete(turma);
    }

    @Transactional
    public void vincularProfessor(UUID idTurma, UUID idProfessor) {
        Turma turma = buscarTurmaOuLancar(idTurma);
        Professor professor = buscarProfessorOuLancar(idProfessor);

        if (turma.getProfessor() != null && turma.getProfessor().getId().equals(professor.getId())) {
            throw new ValidacaoException("Professor já está vinculado à turma.");
        }

        turma.setProfessor(professor);
        turmaRepository.save(turma);
    }

    @Transactional
    public void desvincularProfessor(UUID idTurma, UUID idProfessor) {
        Turma turma = buscarTurmaOuLancar(idTurma);
        Professor professor = buscarProfessorOuLancar(idProfessor);

        if (turma.getProfessor() == null) throw new ValidacaoException("Ainda não há professor vinculado à turma.");
        if (!turma.getProfessor().equals(professor)) throw new ValidacaoException("Professor não está vinculado à turma.");

        turma.setProfessor(null);
        turmaRepository.save(turma);
    }

    @Transactional
    public void vincularAluno(UUID idTurma, UUID idAluno) {
        Turma turma = buscarTurmaOuLancar(idTurma);
        Aluno aluno = buscarAlunoOuLancar(idAluno);

        if (turma.getAlunos().contains(aluno)) {
            throw new ValidacaoException("Aluno já está vinculado à turma.");
        }

        turma.getAlunos().add(aluno);
        turmaRepository.save(turma);
    }

    @Transactional
    public void desvincularAluno(UUID idTurma, UUID idAluno) {
        Turma turma = buscarTurmaOuLancar(idTurma);
        Aluno aluno = buscarAlunoOuLancar(idAluno);

        if (!turma.getAlunos().contains(aluno)) {
            throw new ValidacaoException("Aluno não está vinculado à turma.");
        }

        turma.getAlunos().remove(aluno);
        turmaRepository.save(turma);
    }

    private Turma buscarTurmaOuLancar(UUID idTurma) {
        return turmaRepository.findById(idTurma)
                .orElseThrow(TurmaNaoEncontradaException::new);
    }

    private Professor buscarProfessorOuLancar(UUID idProfessor) {
        return professorRepository.findById(idProfessor)
                .orElseThrow(ProfessorNaoEncontradoException::new);
    }

    private Aluno buscarAlunoOuLancar(UUID idAluno) {
        return alunoRepository.findById(idAluno)
                .orElseThrow(AlunoNaoEncontradoException::new);
    }
}