package com.fighthub.repository;

import com.fighthub.model.Professor;
import com.fighthub.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfessorRepository extends JpaRepository<Professor, UUID> {

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    Page<Professor> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    Optional<Professor> findById(UUID id);

    Optional<Professor> findByUsuario(Usuario usuario);

    boolean existsByUsuarioId(UUID id);

    Optional<Professor> findByUsuarioId(UUID id);

    void deleteByUsuarioId(UUID id);

}
