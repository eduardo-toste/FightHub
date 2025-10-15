package com.fighthub.dto.responsavel;

import com.fighthub.dto.endereco.EnderecoResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ResponsavelDetalhadoResponse(

        UUID id,
        String nome,
        String email,
        String telefone,
        String foto,
        EnderecoResponse endereco

) {
}
