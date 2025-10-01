package com.fighthub.dto.usuario;

import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.model.enums.Role;
import org.hibernate.validator.constraints.br.CPF;

public record UsuarioUpdateParcialRequest(

        String nome,
        String email,
        String foto,
        String telefone,
        @CPF(message = "CPF deve ser válido") String cpf,
        EnderecoRequest endereco,
        Role role,
        Boolean ativo

) {
}
