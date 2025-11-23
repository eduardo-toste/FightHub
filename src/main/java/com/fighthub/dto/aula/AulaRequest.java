package com.fighthub.dto.aula;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AulaRequest(

        @NotBlank(message = "Titulo é obrigatório.")
        String titulo,

        @NotBlank(message = "Descrição é obrigatória.")
        String descricao,

        @NotNull(message = "Data e hora são obrigatórios.")
        LocalDateTime data,

        UUID turmaId,

        @NotNull(message = "Limite de alunos é obrigatório.")
        @Min(1)
        int limiteAlunos

) {
}
