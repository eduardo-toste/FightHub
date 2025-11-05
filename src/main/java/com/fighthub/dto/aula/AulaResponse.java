package com.fighthub.dto.aula;

import java.time.LocalDate;
import java.util.UUID;

public record AulaResponse(

        UUID id,
        String titulo,
        String descricao,
        LocalDate data,
        UUID turmaId,
        int limiteAlunos,
        boolean ativo

) {
}
