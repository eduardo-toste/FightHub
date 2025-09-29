package com.fighthub.dto.usuario;

import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.model.enums.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UsuarioUpdateParcialRequest(

        String nome,
        String email,
        String foto,
        String telefone,
        String cpf,
        EnderecoRequest endereco,
        Role role,
        Boolean ativo

) {
}
