package com.fighthub.service;

import com.fighthub.dto.auth.AtivacaoRequest;
import com.fighthub.exception.TokenInvalidoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.model.Endereco;
import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.TokenType;
import com.fighthub.repository.TokenRepository;
import com.fighthub.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtivacaoService {

    private final TokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final TokenService tokenService;

    @Transactional
    public void ativarConta(AtivacaoRequest request) {
        Token token = tokenRepository.findByToken(request.token())
                .orElseThrow(TokenInvalidoException::new);

        if (jwtService.tokenValido(token.getToken())) {
            throw new ValidacaoException("Token expirado ou revogado");
        }

        Usuario usuario = token.getUsuario();
        atualizarUsuario(usuario, request);

        tokenService.revogarToken(usuario, TokenType.ATIVACAO);

        usuarioRepository.save(usuario);
        tokenRepository.save(token);

        emailService.enviarEmailConfirmacao(usuario);
    }

    private void atualizarUsuario(Usuario usuario, AtivacaoRequest request) {
        Endereco endereco = Endereco.builder()
                .cep(request.endereco().cep())
                .logradouro(request.endereco().logradouro())
                .numero(request.endereco().numero())
                .complemento(request.endereco().complemento())
                .bairro(request.endereco().bairro())
                .cidade(request.endereco().cidade())
                .estado(request.endereco().estado())
                .build();

        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setAtivo(true);
        usuario.setTelefone(request.telefone());
        usuario.setEndereco(endereco);
    }
}
