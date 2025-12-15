package com.fighthub.dto.dashboard;

import java.util.List;

public record EngajamentoDashboardResponse(

        long aulasPrevistasNoMes,
        long aulasRealizadasNoMes,
        long aulasCanceladasNoMes,
        double presencaMediaGeralNoMes,
        double presencaMediaPorTurmaNoMes,
        List<AlunosFaltasResponse> top5AlunosComMaisFaltasNoMes

) {
}
