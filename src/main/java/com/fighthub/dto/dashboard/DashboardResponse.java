package com.fighthub.dto.dashboard;

public record DashboardResponse(

    GeralDashboardResponse dadosGerais,
    TurmasDashboardResponse dadosTurmas

) {
}
