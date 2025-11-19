package com.fighthub.repository;

import com.fighthub.model.Inscricao;
import com.fighthub.model.Presenca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PresencaRepository extends JpaRepository<Presenca, UUID> {

    Optional<Presenca> findByInscricao(Inscricao inscricao);

    Page<Presenca> findAllByInscricaoIn(List<Inscricao> inscricoes, Pageable pageable);
}
