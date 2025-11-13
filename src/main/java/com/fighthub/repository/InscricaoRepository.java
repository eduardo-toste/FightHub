package com.fighthub.repository;

import com.fighthub.model.Aluno;
import com.fighthub.model.Aula;
import com.fighthub.model.Inscricao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InscricaoRepository extends JpaRepository<Inscricao, UUID> {

    Optional<Inscricao> findByAulaAndAluno(Aula aula, Aluno aluno);

}
