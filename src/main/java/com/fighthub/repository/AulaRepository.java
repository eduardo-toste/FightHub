package com.fighthub.repository;

import com.fighthub.model.Aula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AulaRepository extends JpaRepository<Aula, UUID> {
}
