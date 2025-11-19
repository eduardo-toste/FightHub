package com.fighthub.dto.presenca;

import java.time.LocalDate;
import java.util.UUID;

public record PresencaResponse(

        UUID id,
        boolean presente,
        UUID inscricaoId,
        LocalDate dataRegistro

) {
}
