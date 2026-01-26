package com.fighthub.dto.responsavel;

import com.fighthub.dto.endereco.EnderecoResponse;

import java.util.UUID;

public record ResponsavelDetalhadoResponse(

        UUID id,
        String nome,
        String email,
        String telefone,
        String cpf,
        String foto,
        EnderecoResponse endereco

) {
}
