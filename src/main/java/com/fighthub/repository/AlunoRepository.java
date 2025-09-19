package com.fighthub.repository;

import com.fighthub.model.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AlunoRepository extends JpaRepository<Aluno, UUID> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "responsaveis", "responsaveis.usuario"})
    Page<Aluno> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"usuario", "responsaveis", "responsaveis.usuario"})
    Optional<Aluno> findById(UUID id);

}
