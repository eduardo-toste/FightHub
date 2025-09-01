package com.fighthub.service;

import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.TokenType;
import com.fighthub.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    public void salvarToken(Usuario usuario, String jwt) {
        Token token = Token.builder()
                .usuario(usuario)
                .token(jwt)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();

        tokenRepository.save(token);
    }

    public void revogarTokens(Usuario usuario) {
        var tokens = tokenRepository.findAllByUsuarioAndExpiredFalseAndRevokedFalse(usuario);

        tokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(tokens);
    }

}
