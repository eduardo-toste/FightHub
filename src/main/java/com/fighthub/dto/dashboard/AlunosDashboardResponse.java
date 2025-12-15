package com.fighthub.dto.dashboard;

public record AlunosDashboardResponse(

        long totalAlunosAtivos,
        long totalAlunosInativos,
        long novosAlunosUltimos30Dias,
        int idadeMediaAlunos

) {
}
