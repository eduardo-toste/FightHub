package com.fighthub.dto.dashboard;

import com.fighthub.dto.aluno.AlunoResponse;

import java.util.List;

public record EngajamentoDashboardResponse(

        int aulasPrevistasUltimos30Dias,
        int aulasRealizadasUltimos30Dias,
        double percentualAulasRealizadas,
        int aulasCanceladasUltimos30Dias,
        double presencaMediaGeral,
        double presencaMediaPorTurma,
        List<AlunoResponse> top5AlunosMaisEngajados

) {
}
