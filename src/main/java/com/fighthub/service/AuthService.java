package com.fighthub.service;

import com.fighthub.dto.AuthRequest;
import com.fighthub.dto.AuthResponse;
import com.fighthub.exception.TokenInvalidoException;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.model.Usuario;
import com.fighthub.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenService tokenService;

    @Transactional
    public AuthResponse login(AuthRequest request) {
        log.info("Tentando autenticar usuário com e-mail: {}", request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        var usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Usuário com e-mail {} não encontrado", request.email());
                    return new UsuarioNaoEncontradoException();
                });

        tokenService.revogarTokens(usuario);
        var jwtToken = jwtService.gerarToken(usuario);
        var refreshToken = jwtService.gerarRefreshToken(usuario);
        tokenService.salvarTokens(usuario, jwtToken, refreshToken);

        log.info("Login finalizado com sucesso para o usuário: {}", usuario.getEmail());
        return new AuthResponse(jwtToken, refreshToken);
    }

    @Transactional
    public AuthResponse atualizarToken(String refreshToken) {
        log.info("Solicitação de atualização de token recebida");

        if (!jwtService.tokenValido(refreshToken)) {
            log.warn("Refresh token inválido recebido");
            throw new TokenInvalidoException();
        }

        var email = jwtService.extrairEmail(refreshToken);
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Usuário com e-mail {} não encontrado durante atualização de token", email);
                    return new UsuarioNaoEncontradoException();
                });

        tokenService.revogarAccessToken(usuario);

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
            throw new TokenInvalidoException();
        }

        final String jwt = authHeader.substring(7);
        log.info("Logout solicitado para token: {}", jwt);

        tokenService.revogarTokensPorJwt(jwt);

        log.info("Logout concluído para token: {}", jwt);
    }
}