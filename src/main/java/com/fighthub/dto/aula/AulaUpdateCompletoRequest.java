package com.fighthub.dto.aula;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record AulaUpdateCompletoRequest(

        @NotBlank(message = "Titulo é obrigatório.")
        String titulo,

        @NotBlank(message = "Descrição é obrigatória.")
        String descricao,

        @NotNull(message = "Data é obrigatória.")
        LocalDate data,

        @NotNull(message = "Turma é obrigatória.")
        UUID turmaId,

        @NotNull(message = "Limite de alunos é obrigatório.")
        @Min(1)
        int limiteAlunos,

        @NotNull(message = "Status da aula é obrigatório.")
        boolean ativo

) {
}
