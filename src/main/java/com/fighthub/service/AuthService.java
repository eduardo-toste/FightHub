package com.fighthub.service;

import com.fighthub.dto.request.AuthRequest;
import com.fighthub.dto.response.AuthResponse;
import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import com.fighthub.repository.TokenRepository;
import com.fighthub.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenService tokenService;

    @Transactional
    public AuthResponse login(AuthRequest request) {
        log.info("Tentando autenticar usuário com e-mail: {}", request.email());

        var authToken = new UsernamePasswordAuthenticationToken(request.email(), request.senha());
        authenticationManager.authenticate(authToken);

        var usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Usuário com e-mail {} não encontrado", request.email());
                    return new RuntimeException("Usuário não encontrado");
                });

        log.info("Usuário autenticado com sucesso: {} (ID: {})", usuario.getEmail(), usuario.getId());

        tokenService.revogarTokens(usuario);
        log.debug("Tokens antigos revogados para o usuário: {}", usuario.getEmail());

        var jwtToken = jwtService.gerarToken(usuario);
        var refreshToken = jwtService.gerarRefreshToken(usuario);
        log.debug("Novos tokens gerados para o usuário: {}", usuario.getEmail());

        tokenService.salvarTokens(usuario, jwtToken, refreshToken);

        log.info("Login finalizado com sucesso para o usuário: {}", usuario.getEmail());
        return new AuthResponse(jwtToken, refreshToken);
    }

    @Transactional
    public AuthResponse atualizarToken(String refreshToken) {
        log.info("Solicitação de atualização de token recebida");

        if (!jwtService.tokenValido(refreshToken)) {
            log.warn("Refresh token inválido recebido");
            throw new RuntimeException("Refresh token inválido");
        }

        var email = jwtService.extrairEmail(refreshToken);
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Usuário com e-mail {} não encontrado durante atualização de token", email);
                    return new RuntimeException("Usuário não encontrado");
                });

        log.debug("Usuário validado para atualização de token: {}", email);

        var newAccessToken = jwtService.gerarToken(usuario);
        tokenService.salvarAccessToken(usuario, newAccessToken);

        log.info("Token de acesso atualizado com sucesso para o usuário: {}", email);
        return new AuthResponse(newAccessToken, refreshToken);
    }

    @Transactional
    public void logout(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Tentativa de logout com token ausente ou inválido");
            throw new RuntimeException("Token ausente ou inválido.");
        }

        final String jwt = authHeader.substring(7);
        Token accessToken = tokenRepository.findByToken(jwt).orElse(null);

        if (accessToken != null) {
            Usuario usuario = accessToken.getUsuario();
            log.info("Logout iniciado para o usuário: {}", usuario.getEmail());

            var tokensAtivos = tokenRepository.findAllByUsuarioAndExpiredFalseAndRevokedFalse(usuario);

            tokensAtivos.forEach(token -> {
                token.setExpired(true);
                token.setRevoked(true);
            });

            tokenRepository.saveAll(tokensAtivos);

            log.info("Logout finalizado e tokens revogados para o usuário: {}", usuario.getEmail());
        } else {
            log.warn("Token de acesso não encontrado na base: {}", jwt);
        }
    }

}
