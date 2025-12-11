package com.fighthub.dto.dashboard;

public record GeralDashboardResponse(

        long totalAlunosAtivos,
        long totalAlunosInativos,
        long novosAlunosUltimos30Dias,
        int idadeMediaAlunos

) {
}
