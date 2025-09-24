package com.fighthub.repository;

import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    List<Token> findAllByUsuarioAndExpiredFalseAndRevokedFalse(Usuario usuario);

    List<Token> findAllByUsuarioAndRevokedFalseAndTokenType(Usuario usuario, TokenType tokenType);

    Optional<Token> findByToken(String jwt);

    Optional<Token> findByTokenAndTokenType(String jwt, TokenType tokenType);

    Optional<Token> findByTokenAndExpiredFalseAndRevokedFalse(String jwt);

}
