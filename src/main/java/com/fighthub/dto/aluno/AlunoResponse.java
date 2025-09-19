package com.fighthub.dto.aluno;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AlunoResponse(
        UUID id,
        String nome,
        String email,
        String foto,
        boolean ativo,
        LocalDate dataNascimento,
        LocalDate dataMatricula,
        List<ResponsavelResumo> responsaveis
) {
    public record ResponsavelResumo(
            UUID id,
            String nome,
            String email
    ) {}
}