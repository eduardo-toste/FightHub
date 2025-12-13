package com.fighthub.dto.dashboard;

public record DashboardResponse(

    AlunosDashboardResponse dadosGerais,
    TurmasDashboardResponse dadosTurmas,
    EngajamentoDashboardResponse dadosEngajamento

) {
}
