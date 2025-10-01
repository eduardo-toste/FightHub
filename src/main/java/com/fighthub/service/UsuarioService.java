package com.fighthub.service;

import com.fighthub.dto.usuario.*;
import com.fighthub.exception.TokenInvalidoException;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.mapper.UsuarioMapper;
import com.fighthub.model.Token;
import com.fighthub.model.Usuario;
import com.fighthub.repository.TokenRepository;
import com.fighthub.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public Page<UsuarioResponse> obterTodosUsuarios(Pageable pageable) {
        return UsuarioMapper.toPage(usuarioRepository.findAll(pageable));
    }

    public UsuarioDetalhadoResponse obterUsuario(UUID id) {
        return UsuarioMapper.toDetailedDTO(usuarioRepository.findById(id)
                .orElseThrow(UsuarioNaoEncontradoException::new));
    }

    public UsuarioResponse updateRole(UUID id, UpdateRoleRequest request) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        if (usuario.getRole().equals(request.role())) {
            throw new ValidacaoException("Usuário já cadastrado como " + request.role());
        }

        usuario.setRole(request.role());
        usuarioRepository.save(usuario);

        return UsuarioMapper.toDTO(usuario);
    }

    public UsuarioResponse updateStatus(UUID id, UpdateStatusRequest request) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        if (usuario.isAtivo() == request.usuarioAtivo()) {
            if (request.usuarioAtivo()) {
                throw new ValidacaoException("Usuário já está ativo");
            } else {
                throw new ValidacaoException("Usuário já está inativo");
            }
        }

        usuario.setAtivo(request.usuarioAtivo());
        usuarioRepository.save(usuario);

        return UsuarioMapper.toDTO(usuario);
    }

    public UsuarioDetalhadoResponse updateUsuarioCompleto(UUID id, UsuarioUpdateCompletoRequest request) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        usuario.putUpdate(request);
        usuarioRepository.save(usuario);

        return UsuarioMapper.toDetailedDTO(usuario);
    }

    public UsuarioDetalhadoResponse updateUsuarioParcial(UUID id, UsuarioUpdateParcialRequest request) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);

        usuario.patchUpdate(request);
        usuarioRepository.save(usuario);

        return UsuarioMapper.toDetailedDTO(usuario);
    }

    public UsuarioDetalhadoResponse obterDadosDoProprioUsuario(HttpServletRequest request) {
        Usuario usuario = obterUsuarioPorEmail(request);
        return UsuarioMapper.toDetailedDTO(usuario);
    }

    public UsuarioDetalhadoResponse updateProprioCompleto(HttpServletRequest request, UsuarioUpdateCompletoRequest updateRequest) {
        Usuario usuario = obterUsuarioPorEmail(request);

        usuario.putUpdate(updateRequest);
        usuarioRepository.save(usuario);

        return UsuarioMapper.toDetailedDTO(usuario);
    }

    public UsuarioDetalhadoResponse updateProprioParcial(HttpServletRequest request, UsuarioUpdateParcialRequest updateRequest) {
        Usuario usuario = obterUsuarioPorEmail(request);

        usuario.patchUpdate(updateRequest);
        usuarioRepository.save(usuario);

        return UsuarioMapper.toDetailedDTO(usuario);
    }

    public void updateSenha(HttpServletRequest request, UpdateSenhaRequest updateRequest) {
        Usuario usuario = obterUsuarioPorEmail(request);
        usuario.setSenha(passwordEncoder.encode(updateRequest.senha()));
        usuarioRepository.save(usuario);
    }

    private Usuario obterUsuarioPorEmail(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt = authHeader.substring(7);
        final String emailUsuario = jwtService.extrairEmail(jwt);

        return usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(UsuarioNaoEncontradoException::new);
    }
}
