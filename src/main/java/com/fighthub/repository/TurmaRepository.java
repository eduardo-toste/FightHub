package com.fighthub.repository;

import com.fighthub.model.Aluno;
import com.fighthub.model.Professor;
import com.fighthub.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TurmaRepository extends JpaRepository<Turma, UUID> {

    List<Turma> findAllByAlunos(Aluno aluno);

    List<Turma> findAllByProfessor(Professor professor);

    long countByAtivo(boolean ativo);

}
