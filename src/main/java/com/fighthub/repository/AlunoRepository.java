package com.fighthub.repository;

import com.fighthub.model.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlunoRepository extends JpaRepository<Aluno, UUID> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "turmas", "turmas.alunos"})
    Page<Aluno> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"usuario", "responsaveis", "responsaveis.usuario"})
    Optional<Aluno> findById(UUID id);

    boolean existsByUsuarioId(UUID id);

    void deleteByUsuarioId(UUID id);

    @EntityGraph(attributePaths = {"usuario", "responsaveis", "responsaveis.usuario"})
    Optional<Aluno> findByUsuarioId(UUID id);

    long countByMatriculaAtiva(boolean statusMatricula);

    long countByMatriculaAtivaAndDataMatriculaAfter(boolean statusMatricula, LocalDate data);

    @Query(value = "SELECT SUM(date_part('year', age(current_date, a.data_nascimento))) " +
            "FROM alunos a WHERE a.matricula_ativa = :status", nativeQuery = true)
    long sumAgesByMatriculaAtiva(@Param("status") boolean status);

    @Query("""
            SELECT a FROM Aluno a
            WHERE a.dataNascimento > :dateLimite
            AND (a.responsaveis IS EMPTY OR SIZE(a.responsaveis) = 0)
            AND a.matriculaAtiva = true
            ORDER BY a.usuario.nome
            """)
    @EntityGraph(attributePaths = {"usuario", "responsaveis", "responsaveis.usuario"})
    List<Aluno> findMenoresSemResponsavel(@Param("dateLimite") LocalDate dateLimite);

}
