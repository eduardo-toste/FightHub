package com.fighthub.repository;

import com.fighthub.model.Modalidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ModalidadeRepository extends JpaRepository<Modalidade, UUID> {

}
