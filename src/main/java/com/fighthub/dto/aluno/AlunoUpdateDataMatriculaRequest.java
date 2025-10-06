package com.fighthub.dto.aluno;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AlunoUpdateDataMatriculaRequest(

        @NotNull(message = "Data de matricula é obrigatória, com o formato AAAA-MM-DD")
        LocalDate dataMatricula

) {
}
