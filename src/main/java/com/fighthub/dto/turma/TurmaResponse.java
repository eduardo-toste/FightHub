package com.fighthub.dto.turma;

import java.util.UUID;

public record TurmaResponse(

        UUID id,
        String nome,
        String horario,
        UUID professorId,
        boolean ativo

) {
}
