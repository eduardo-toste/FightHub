package com.fighthub.dto.turma;

import jakarta.validation.constraints.NotNull;

public record TurmaUpdateStatusRequest(

        @NotNull(message = "Status é obrigatório.")
        boolean ativo

) {
}
