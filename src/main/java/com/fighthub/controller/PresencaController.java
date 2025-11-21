// java
package com.fighthub.controller;

import com.fighthub.dto.presenca.PresencaRequest;
import com.fighthub.dto.presenca.PresencaResponse;
import com.fighthub.service.PresencaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Presenças", description = "Endpoints para consulta e atualização de presenças")
public class PresencaController {

    private final PresencaService presencaService;

    @Operation(summary = "Atualizar status de presença por inscrição",
               description = "Atualiza o status de presença (presente/ausente) para uma inscrição específica em uma aula.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado"),
            @ApiResponse(responseCode = "404", description = "Aula ou inscrição não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor")
    })
    @PatchMapping("/aulas/{idAula}/presencas/inscricao/{idInscricao}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> atualizarStatusPresenca(
            @Parameter(description = "ID da aula", required = true) @PathVariable UUID idAula,
            @Parameter(description = "ID da inscrição", required = true) @PathVariable UUID idInscricao,
            @RequestBody @Valid PresencaRequest request,
            HttpServletRequest httpServletRequest) {
        presencaService.atualizarStatusPresencaPorInscricao(idAula, idInscricao, request, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Listar presenças por aula",
               description = "Retorna uma página de presenças para a aula informada. Professores só podem ver presenças da sua aula.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de presenças retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado"),
            @ApiResponse(responseCode = "404", description = "Aula não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor")
    })
    @GetMapping("/aulas/{idAula}/presencas")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Page<PresencaResponse>> listarPresencasPorAula(
            @Parameter(description = "ID da aula", required = true) @PathVariable UUID idAula,
            @Parameter(description = "Parâmetros de paginação") Pageable pageable,
            HttpServletRequest httpServletRequest) {
        var presencas = presencaService.listarPresencasPorAula(idAula, pageable, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(presencas);
    }

    @Operation(summary = "Listar minhas presenças",
               description = "Retorna uma página com as presenças do aluno autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de presenças do aluno retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso recusado"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor")
    })
    @GetMapping("/aulas/me/presencas")
    @PreAuthorize("hasAnyRole('ALUNO')")
    public ResponseEntity<Page<PresencaResponse>> listarMinhasPresencas(
            @Parameter(description = "Parâmetros de paginação") Pageable pageable,
            HttpServletRequest httpServletRequest) {
        var presencas = presencaService.listarMinhasPresencas(pageable, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(presencas);
    }
}