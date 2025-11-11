package com.fighthub.repository;

import com.fighthub.model.Aula;
import com.fighthub.model.Turma;
import com.fighthub.model.enums.ClassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AulaRepository extends JpaRepository<Aula, UUID> {

    Page<Aula> findByStatusAndTurmaIn(ClassStatus status, List<Turma> turmas, Pageable pageable);

}
