package com.fighthub.service;

import com.fighthub.dto.usuario.*;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.exception.ValidacaoException;
import com.fighthub.mapper.UsuarioMapper;
import com.fighthub.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

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
}
