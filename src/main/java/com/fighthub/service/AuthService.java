package com.fighthub.service;

import com.fighthub.dto.request.AuthRequest;
import com.fighthub.dto.request.RegisterRequest;
import com.fighthub.dto.response.AuthResponse;
import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import com.fighthub.repository.TokenRepository;
import com.fighthub.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenService tokenService;

    @Transactional
    public AuthResponse cadastrar(RegisterRequest request) {
        var usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .role(request.role())
                .ativo(true)
                .loginSocial(false)
                .build();

        usuarioRepository.save(usuario);

        var jwtToken = jwtService.gerarToken(usuario);
        var refreshToken = jwtService.gerarRefreshToken(usuario);

        tokenService.salvarToken(usuario, jwtToken);

        return new AuthResponse(jwtToken, refreshToken);
    }

    @Transactional
    public AuthResponse autenticar(AuthRequest request) {
        var authToken = new UsernamePasswordAuthenticationToken(request.email(), request.senha());
        authenticationManager.authenticate(authToken);

        var usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        tokenService.revogarTokens(usuario);

        var jwtToken = jwtService.gerarToken(usuario);
        var refreshToken = jwtService.gerarRefreshToken(usuario);

        tokenService.salvarToken(usuario, jwtToken);

        return new AuthResponse(jwtToken, refreshToken);
    }

    @Transactional
    public AuthResponse atualizarToken(String refreshToken) {
        if (!jwtService.tokenValido(refreshToken)) {
            throw new RuntimeException("Refresh token inválido");
        }

        var email = jwtService.extrairEmail(refreshToken);
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        var newAccessToken = jwtService.gerarToken(usuario);

        tokenService.salvarToken(usuario, newAccessToken);

        return new AuthResponse(newAccessToken, refreshToken);
    }

    @Transactional
    public void logout(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token ausente ou inválido.");
        }

        final String jwt = authHeader.substring(7);
        Token tokenArmazenado = tokenRepository.findByToken(jwt).orElse(null);
        if (tokenArmazenado != null) {
            tokenArmazenado.setExpired(true);
            tokenArmazenado.setRevoked(true);
            tokenRepository.save(tokenArmazenado);
        }
    }

}
