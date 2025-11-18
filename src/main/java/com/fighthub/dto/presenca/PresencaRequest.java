package com.fighthub.dto.presenca;

import jakarta.validation.constraints.NotNull;

public record PresencaRequest(

        @NotNull(message = "Presença é obrigatória.")
        boolean presente

) {
}
