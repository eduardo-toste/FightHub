package com.fighthub.service;

import com.fighthub.dto.usuario.UsuarioDetalhadoResponse;
import com.fighthub.dto.usuario.UsuarioResponse;
import com.fighthub.exception.UsuarioNaoEncontradoException;
import com.fighthub.mapper.UsuarioMapper;
import com.fighthub.repository.UsuarioRepository;
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
}
