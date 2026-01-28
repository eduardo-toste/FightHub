package com.fighthub.repository;

import com.fighthub.model.Aluno;
import com.fighthub.model.Professor;
import com.fighthub.model.Turma;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TurmaRepository extends JpaRepository<Turma, UUID> {

    List<Turma> findAllByAlunos(Aluno aluno);

    List<Turma> findAllByProfessor(Professor professor);

    long countByAtivo(boolean ativo);

    @EntityGraph(attributePaths = {"professor", "professor.usuario"})
    Page<Turma> findAll(Pageable pageable);

    @Query("SELECT COUNT(a.id) FROM Turma t LEFT JOIN t.alunos a WHERE t.id = :turmaId")
    long countAlunosByTurmaId(@Param("turmaId") UUID turmaId);

}
