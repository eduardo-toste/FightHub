package com.fighthub.service;

import com.fighthub.dto.auth.*;
import com.fighthub.exception.TokenInvalidoException;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.TokenType;
import com.fighthub.repository.TokenRepository;
import com.fighthub.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

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
    public RefreshTokenResponse atualizarToken(String refreshToken) {
        log.info("Solicitação de atualização de token recebida");

        if (!jwtService.tokenValido(refreshToken)) {
            log.warn("Refresh token inválido recebido");
            throw new TokenInvalidoException();
        }

        tokenRepository.findByTokenAndTokenType(refreshToken, TokenType.REFRESH)
                .orElseThrow(() -> new ValidacaoException("O token recebido não é do tipo REFRESH"));

        var email = jwtService.extrairEmail(refreshToken);
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Usuário com e-mail {} não encontrado durante atualização de token", email);
                    return new UsuarioNaoEncontradoException();
                });

        tokenService.revogarToken(usuario, TokenType.ACCESS);

        var newAccessToken = jwtService.gerarToken(usuario);
        tokenService.salvarAccessToken(usuario, newAccessToken);

        log.info("Token de acesso atualizado com sucesso para o usuário: {}", email);
        return new RefreshTokenResponse(newAccessToken);
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

    @Transactional
    public void recoverPassword(RecuperarSenhaRequest request) {
        var usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(UsuarioNaoEncontradoException::new);

        tokenService.revogarToken(usuario, TokenType.RECUPERACAO_SENHA);
        var codigoRecuperacao = tokenService.salvarCodigoRecuperacao(usuario);
        emailService.enviarEmailRecuperacaoSenha(usuario, codigoRecuperacao);
    }

    @Transactional
    public void validateRecoverCode(ValidarCodigoRecuperacaoRequest request) {
        var usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(UsuarioNaoEncontradoException::new);

        validarCodigo(usuario, request.codigoRecuperacao());
    }

    @Transactional
    public void confirmarRecuperacaoSenha(ConfirmarRecuperacaoSenhaRequest request) {
        var usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(UsuarioNaoEncontradoException::new);

        validarCodigo(usuario, request.codigoRecuperacao());

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);
        tokenService.revogarToken(usuario, TokenType.RECUPERACAO_SENHA);
    }

    private void validarCodigo(Usuario usuario, String codigo) {
        if (!tokenService.validarCodigoRecuperacao(usuario, codigo)) {
            throw new TokenInvalidoException();
        }
    }
}