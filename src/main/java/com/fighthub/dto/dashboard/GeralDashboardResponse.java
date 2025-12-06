package com.fighthub.dto.dashboard;

public record GeralDashboardResponse(

        int totalAlunosAtivos,
        int totalAlunosInativos,
        int novosAlunosUltimos30Dias,
        int alunosQueSairamUltimos30Dias,
        int idadeMediaAlunos

) {
}
