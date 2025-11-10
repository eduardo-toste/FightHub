package com.fighthub.dto.aula;

import com.fighthub.model.enums.ClassStatus;

import java.time.LocalDate;
import java.util.UUID;

public record AulaResponse(

        UUID id,
        String titulo,
        String descricao,
        LocalDate data,
        UUID turmaId,
        int limiteAlunos,
        ClassStatus status,
        boolean ativo

) {
}
