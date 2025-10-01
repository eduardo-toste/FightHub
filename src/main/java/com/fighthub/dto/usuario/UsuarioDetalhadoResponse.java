package com.fighthub.dto.usuario;

import com.fighthub.dto.endereco.EnderecoResponse;
import com.fighthub.model.enums.Role;

import java.util.UUID;

public record UsuarioDetalhadoResponse(

        UUID id,
        String nome,
        String cpf,
        String email,
        String telefone,
        String foto,
        Role role,
        boolean loginSocial,
        boolean ativo,
        EnderecoResponse endereco

) {
}
