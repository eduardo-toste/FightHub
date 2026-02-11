package com.fighthub.dto.inscricao;

import com.fighthub.model.enums.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record InscricaoResponse(

        UUID id,
        UUID alunoId,
        UUID aulaId,
        String aulaTitulo,
        String aulaDescricao,
        LocalDateTime aulaData,
        String turmaNome,
        int limiteAlunos,
        SubscriptionStatus status,
        LocalDateTime inscritoEm

) {
}
