package com.fighthub.dto.usuario;

import com.fighthub.model.enums.Role;

import java.util.UUID;

public record UsuarioResponse(

        UUID id,
        String nome,
        String cpf,
        String email,
        String telefone,
        Role role,
        boolean ativo


) {
}
