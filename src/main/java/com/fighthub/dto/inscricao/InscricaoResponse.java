package com.fighthub.dto.inscricao;

import com.fighthub.model.enums.SubscriptionStatus;

import java.time.LocalDate;
import java.util.UUID;

public record InscricaoResponse(

        UUID id,
        UUID alunoId,
        UUID aulaId,
        SubscriptionStatus status,
        LocalDate dataInscricao

) {
}
