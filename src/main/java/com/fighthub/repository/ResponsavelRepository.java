package com.fighthub.repository;

import com.fighthub.model.Responsavel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResponsavelRepository extends JpaRepository<Responsavel, UUID> {
}
