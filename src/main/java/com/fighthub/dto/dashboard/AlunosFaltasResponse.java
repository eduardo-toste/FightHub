package com.fighthub.dto.dashboard;

import java.util.UUID;

public record AlunosFaltasResponse(

        UUID alunoId,
        String nome,
        long faltas

) {
}
