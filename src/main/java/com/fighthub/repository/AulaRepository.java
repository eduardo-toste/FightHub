package com.fighthub.repository;

import com.fighthub.model.Aula;
import com.fighthub.model.Turma;
import com.fighthub.model.enums.ClassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AulaRepository extends JpaRepository<Aula, UUID> {

    Page<Aula> findByStatusAndTurmaIn(ClassStatus status, List<Turma> turmas, Pageable pageable);

    @Query(value = """
        SELECT AVG(ocupacao)
        FROM (
            SELECT
                a.id,
                COUNT(p.id) * 1.0 / a.limite_alunos AS ocupacao
            FROM aulas a
            LEFT JOIN inscricoes i ON i.aula_id = a.id
            LEFT JOIN presencas p ON p.inscricao_id = i.id AND p.presente = true
            WHERE a.ativo = true
            GROUP BY a.id, a.limite_alunos
        ) AS sub
    """, nativeQuery = true)
    Double calcularOcupacaoMediaAulas();

    @Query(value = """
        SELECT CASE WHEN COUNT(*) = 0 THEN 0.0
            ELSE 100.0 * SUM(CASE WHEN sub.ocupacao > 0.9 THEN 1 ELSE 0 END) / COUNT(*)
        END
        FROM (
            SELECT
                a.id,
                COALESCE(CAST(COUNT(p.id) AS double precision) / NULLIF(a.limite_alunos, 0), 0) AS ocupacao
            FROM aulas a
            LEFT JOIN inscricoes i ON i.aula_id = a.id
            LEFT JOIN presencas p ON p.inscricao_id = i.id AND p.presente = true
            WHERE a.ativo = true
            GROUP BY a.id, a.limite_alunos
        ) AS sub
    """, nativeQuery = true)
    Double calcularPercentualAulasLotadas();

    @Query(value = """
        SELECT CASE WHEN COUNT(*) = 0 THEN 0.0 ELSE AVG(cnt) END
        FROM (
            SELECT COALESCE(CAST(COUNT(p.id) AS double precision), 0.0) AS cnt
            FROM aulas a
            LEFT JOIN inscricoes i ON i.aula_id = a.id
            LEFT JOIN presencas p ON p.inscricao_id = i.id AND p.presente = true
            WHERE a.ativo = true
            GROUP BY a.id
        ) AS sub
    """, nativeQuery = true)
    Double calcularMediaAlunosPorAula();

}