package com.fighthub.dto.professor;

import com.fighthub.dto.endereco.EnderecoResponse;

import java.util.UUID;

public record ProfessorDetalhadoResponse(

        UUID id,
        String nome,
        String email,
        String telefone,
        String cpf,
        String foto,
        EnderecoResponse endereco

) {
}
