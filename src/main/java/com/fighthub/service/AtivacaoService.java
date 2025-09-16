package com.fighthub.service;

import com.fighthub.exception.TokenInvalidoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import com.fighthub.repository.TokenRepository;
import com.fighthub.repository.UsuarioRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AtivacaoService {

    private final TokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void ativarConta(String tokenStr, String novaSenha) {
        Token token = tokenRepository.findByToken(tokenStr)
                .orElseThrow(TokenInvalidoException::new);

        if (token.isExpired() || token.isRevoked() || token.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new ValidacaoException("Token expirado ou revogado");
        }

        Usuario usuario = token.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setAtivo(true);

        usuarioRepository.save(usuario);

        token.setExpired(true);
        token.setRevoked(true);
        tokenRepository.save(token);
    }
}
