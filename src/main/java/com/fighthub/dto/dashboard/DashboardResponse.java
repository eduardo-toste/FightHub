package com.fighthub.dto.dashboard;

public record DashboardResponse(

    AlunosDashboardResponse dadosAlunos,
    TurmasDashboardResponse dadosTurmas,
    EngajamentoDashboardResponse dadosEngajamento

) {
}
