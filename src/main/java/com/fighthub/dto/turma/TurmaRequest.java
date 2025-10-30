package com.fighthub.dto.turma;

import java.util.UUID;

public record TurmaRequest(

    String nome,
    String horario,
    UUID professorId

) {
}
