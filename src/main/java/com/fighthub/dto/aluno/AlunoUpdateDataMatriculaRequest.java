package com.fighthub.dto.aluno;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record AlunoUpdateDataMatriculaRequest(

        @NotBlank(message = "Data de matricula é obrigatória, com o formato AAAA-MM-DD")
        LocalDate dataMatricula

) {
}
