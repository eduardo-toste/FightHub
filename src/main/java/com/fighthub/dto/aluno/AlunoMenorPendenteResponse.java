package com.fighthub.dto.aluno;

import java.util.UUID;

public record AlunoMenorPendenteResponse(
        UUID id,
        String nome,
        String email,
        String dataNascimento
) {}
