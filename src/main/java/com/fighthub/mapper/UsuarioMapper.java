package com.fighthub.mapper;

import com.fighthub.dto.endereco.EnderecoResponse;
import com.fighthub.dto.usuario.UsuarioDetalhadoResponse;
import com.fighthub.dto.usuario.UsuarioResponse;
import com.fighthub.model.Usuario;
import org.springframework.data.domain.Page;

public class UsuarioMapper {

    public static UsuarioResponse toDTO(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getRole(),
                usuario.isAtivo()
        );
    }

    public static UsuarioDetalhadoResponse toDetailedDTO(Usuario usuario) {
        return new UsuarioDetalhadoResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getFoto(),
                usuario.getRole(),
                usuario.isLoginSocial(),
                usuario.isAtivo(),
                EnderecoResponse.fromEntity(usuario.getEndereco())
        );
    }

    public static Page<UsuarioResponse> toPage(Page<Usuario> page) {
        return page.map(UsuarioMapper::toDTO);
    }

}
