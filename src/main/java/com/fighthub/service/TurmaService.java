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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;

    public void criarTurma(TurmaRequest request) {
        Professor professor = buscarProfessorOuLancar(request.professorId());
        turmaRepository.save(TurmaMapper.toEntity(request, professor));
    }

    public Page<TurmaResponse> buscarTurmas(Pageable pageable) {
        return TurmaMapper.toPageDTO(turmaRepository.findAll(pageable));
    }

    public TurmaResponse buscarTurmaPorId(UUID id) {
        Turma turma = buscarTurmaOuLancar(id);
        return TurmaMapper.toDTO(turma);
    }

    public TurmaResponse atualizarTurma(UUID id, TurmaUpdateCompletoRequest request) {
        Turma turma = buscarTurmaOuLancar(id);
        Professor professor = buscarProfessorOuLancar(request.professorId());
        turma.putUpdate(request, professor);
        return TurmaMapper.toDTO(turmaRepository.save(turma));
    }

    public TurmaResponse atualizarStatusTurma(UUID id, TurmaUpdateStatusRequest request) {
        Turma turma = buscarTurmaOuLancar(id);
        turma.setAtivo(request.ativo());
        return TurmaMapper.toDTO(turmaRepository.save(turma));
    }

    public void excluirTurma(UUID id) {
        Turma turma = buscarTurmaOuLancar(id);
        turmaRepository.delete(turma);
    }

    public TurmaResponse vincularProfessor(UUID idTurma, UUID idProfessor) {
        Turma turma = buscarTurmaOuLancar(idTurma);
        Professor professor = buscarProfessorOuLancar(idProfessor);

        if (turma.getProfessor().equals(professor)) {
            throw new ValidacaoException("Professor já é o responsável pela turma.");
        }

        turma.setProfessor(professor);
        return TurmaMapper.toDTO(turmaRepository.save(turma));
    }

    public TurmaResponse desvincularProfessor(UUID idTurma, UUID idProfessor) {
        Turma turma = buscarTurmaOuLancar(idTurma);
        Professor professor = buscarProfessorOuLancar(idProfessor);

        if (!turma.getProfessor().equals(professor)) {
            throw new ValidacaoException("Professor não está vinculado à turma.");
        }

        turma.setProfessor(null);
        return TurmaMapper.toDTO(turmaRepository.save(turma));
    }

    public TurmaResponse vincularAluno(UUID idTurma, UUID idAluno) {
        Turma turma = buscarTurmaOuLancar(idTurma);
        Aluno aluno = buscarAlunoOuLancar(idAluno);

        if (turma.getAlunos().contains(aluno)) {
            throw new ValidacaoException("Aluno já está vinculado à turma.");
        }

        turma.getAlunos().add(aluno);
        return TurmaMapper.toDTO(turmaRepository.save(turma));
    }

    public TurmaResponse desvincularAluno(UUID idTurma, UUID idAluno) {
        Turma turma = buscarTurmaOuLancar(idTurma);
        Aluno aluno = buscarAlunoOuLancar(idAluno);

        if (!turma.getAlunos().contains(aluno)) {
            throw new ValidacaoException("Aluno não está vinculado à turma.");
        }

        turma.getAlunos().remove(aluno);
        return TurmaMapper.toDTO(turmaRepository.save(turma));
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