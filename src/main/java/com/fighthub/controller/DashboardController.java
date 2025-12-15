// java
package com.fighthub.controller;

import com.fighthub.dto.dashboard.DashboardResponse;
import com.fighthub.exception.dto.ErrorResponse;
import com.fighthub.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints para dados do dashboard administrativo")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Dados do Dashboard", description = "Retorna dados agregados para a visão administrativa do sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do dashboard retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR')")
    public ResponseEntity<DashboardResponse> buscarDadosDashboard() {
        DashboardResponse dashboardData = dashboardService.getDashboardData();
        return ResponseEntity.status(HttpStatus.OK).body(dashboardData);
    }
}