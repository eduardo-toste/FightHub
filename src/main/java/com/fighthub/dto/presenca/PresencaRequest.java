package com.fighthub.dto.presenca;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PresencaRequest(

        @NotNull(message = "Inscrição é obrigatória.")
        UUID inscricaoId,

        @NotNull(message = "Presença é obrigatória.")
        boolean presente

) {
}
