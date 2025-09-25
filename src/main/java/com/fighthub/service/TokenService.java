package com.fighthub.service;

import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.TokenType;
import com.fighthub.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;

    public void salvarTokens(Usuario usuario, String accessToken, String refreshToken) {
        var agora = LocalDateTime.now();

        Token tokenAccess = Token.builder()
                .usuario(usuario)
                .token(accessToken)
                .tokenType(TokenType.ACCESS)
                .expired(false)
                .revoked(false)
                .criadoEm(agora)
                .expiraEm(agora.plusHours(1))
                .build();

        Token tokenRefresh = Token.builder()
                .usuario(usuario)
                .token(refreshToken)
                .tokenType(TokenType.REFRESH)
                .expired(false)
                .revoked(false)
                .criadoEm(agora)
                .expiraEm(agora.plusDays(7))
                .build();

        tokenRepository.saveAll(List.of(tokenAccess, tokenRefresh));
        log.debug("Tokens de acesso e refresh salvos para usuário {}", usuario.getEmail());
    }

    public void salvarAccessToken(Usuario usuario, String accessToken) {
        var agora = LocalDateTime.now();

        Token token = Token.builder()
                .usuario(usuario)
                .token(accessToken)
                .tokenType(TokenType.ACCESS)
                .expired(false)
                .revoked(false)
                .criadoEm(agora)
                .expiraEm(agora.plusHours(1))
                .build();

        tokenRepository.save(token);
        log.debug("Novo token de acesso salvo para usuário {}", usuario.getEmail());
    }

    public String salvarTokenAtivacao(Usuario usuario) {
        var agora = LocalDateTime.now();

        String tokenJwt = jwtService.gerarTokenAtivacao(usuario);

        Token token = Token.builder()
                .usuario(usuario)
                .token(tokenJwt)
                .tokenType(TokenType.ATIVACAO)
                .expired(false)
                .revoked(false)
                .criadoEm(agora)
                .expiraEm(agora.plusDays(1))
                .build();

        tokenRepository.save(token);
        return tokenJwt;
    }

    public void revogarTokens(Usuario usuario) {
        var tokens = tokenRepository.findAllByUsuarioAndExpiredFalseAndRevokedFalse(usuario);

        tokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(tokens);
        log.debug("{} tokens ativos revogados para usuário {}", tokens.size(), usuario.getEmail());
    }

    public void revogarTokensPorJwt(String jwt) {
        tokenRepository.findByToken(jwt).ifPresent(accessToken -> {
            Usuario usuario = accessToken.getUsuario();
            var tokensAtivos = tokenRepository.findAllByUsuarioAndExpiredFalseAndRevokedFalse(usuario);

            tokensAtivos.forEach(token -> {
                token.setExpired(true);
                token.setRevoked(true);
            });

            tokenRepository.saveAll(tokensAtivos);
            log.debug("{} tokens revogados para usuário {} via JWT", tokensAtivos.size(), usuario.getEmail());
        });
    }

    public void revogarToken(Usuario usuario, TokenType tipo) {
        var tokens = tokenRepository.findAllByUsuarioAndRevokedFalseAndTokenType(usuario, tipo);

        if (tokens.isEmpty()) {
            log.debug("Nenhum token ativo encontrado para o usuário: {}", usuario.getEmail());
            return;
        }

        tokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(tokens);
        log.info("Revogados {} token(s) para o usuário: {}", tokens.size(), usuario.getEmail());
    }
}