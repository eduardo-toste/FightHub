package com.fighthub.dto.dashboard;

import com.fighthub.model.Aluno;

import java.util.List;

public record EngajamentoDashboardResponse(

        int aulasPrevistasUltimos30Dias,
        int aulasRealizadasUltimos30Dias,
        double percentualAulasRealizadas,
        int aulasCanceladasUltimos30Dias,
        double presencaMediaGeral,
        double presencaMediaPorTurma,
        List<Aluno> top5AlunosMaisEngajados

) {
}
