package com.fighthub.dto.aluno;

import jakarta.validation.constraints.NotBlank;

public record AlunoUpdateMatriculaRequest(

        @NotBlank(message = "Situação da matricula é obrigatória")
        boolean matriculaAtiva

) {
}
