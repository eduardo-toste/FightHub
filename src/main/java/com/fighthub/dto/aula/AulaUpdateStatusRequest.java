package com.fighthub.dto.aula;

import com.fighthub.model.enums.ClassStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

public record AulaUpdateStatusRequest(

        @Enumerated(EnumType.STRING)
        @NotNull(message = "Status da aula é obrigatório.")
        ClassStatus status

) {
}
