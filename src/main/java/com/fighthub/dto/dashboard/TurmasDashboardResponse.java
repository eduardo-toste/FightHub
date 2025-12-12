package com.fighthub.dto.dashboard;

public record TurmasDashboardResponse(

        long totalTurmasAtivas,
        long totalTurmasInativas,
        double ocupacaoMediaTurmas

) {
}
