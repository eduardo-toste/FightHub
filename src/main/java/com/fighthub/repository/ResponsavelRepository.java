package com.fighthub.repository;

import com.fighthub.model.Aluno;
import com.fighthub.model.Responsavel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResponsavelRepository extends JpaRepository<Responsavel, UUID> {

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    Page<Responsavel> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    Optional<Responsavel> findById(UUID id);

}
