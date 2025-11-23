package com.fighthub.repository;

import com.fighthub.model.Aluno;
import com.fighthub.model.Aula;
import com.fighthub.model.Inscricao;
import com.fighthub.model.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InscricaoRepository extends JpaRepository<Inscricao, UUID> {

    Optional<Inscricao> findByAulaAndAluno(Aula aula, Aluno aluno);

    Page<Inscricao> findAllByAula(Aula aula, Pageable pageable);

    Page<Inscricao> findAllByAlunoAndStatus(Aluno aluno, SubscriptionStatus status, Pageable pageable);

    List<Inscricao> findAllByAulaAndStatus(Aula aula, SubscriptionStatus status);
}
