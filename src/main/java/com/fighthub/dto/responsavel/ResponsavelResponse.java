package com.fighthub.dto.responsavel;

import java.util.UUID;

public record ResponsavelResponse(

        UUID id,
        String nome,
        String email,
        String telefone,
        String cpf,
        String foto

) {
}
