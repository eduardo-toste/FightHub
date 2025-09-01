package com.fighthub.repository;

import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    List<Token> findAllByUsuarioAndExpiredFalseAndRevokedFalse(Usuario usuario);

    Optional<Token> findByToken(String jwt);

}
