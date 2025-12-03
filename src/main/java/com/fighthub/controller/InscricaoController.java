package com.fighthub.controller;

import com.fighthub.dto.inscricao.InscricaoResponse;
import com.fighthub.service.InscricaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
@Tag(name = "Inscrições", description = "Operações relacionadas às inscrições em aulas")
public class InscricaoController {

    private final InscricaoService inscricaoService;

    @Operation(summary = "Inscrever aluno", description = "Inscreve o usuário autenticado na aula especificada pelo idAula")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inscrição criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aula não encontrada", content = @Content)
    })
    @PostMapping("/aulas/{idAula}/inscricoes")
    @PreAuthorize("hasAnyRole('ALUNO')")
    public ResponseEntity<Void> inscreverAluno(
            @Parameter(description = "ID da aula", required = true) @PathVariable UUID idAula,
            HttpServletRequest request) {
        inscricaoService.inscreverAluno(idAula, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Cancelar inscrição", description = "Cancela a inscrição do usuário autenticado na aula especificada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Inscrição cancelada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Inscrição ou aula não encontrada", content = @Content)
    })
    @DeleteMapping("/aulas/{idAula}/inscricoes")
    @PreAuthorize("hasAnyRole('ALUNO')")
    public ResponseEntity<Void> cancelarInscricaoAluno(
            @Parameter(description = "ID da aula", required = true) @PathVariable UUID idAula,
            HttpServletRequest request) {
        inscricaoService.cancelarInscricao(idAula, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar inscrições por aula", description = "Retorna uma página com as inscrições da aula")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inscrições retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InscricaoResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aula não encontrada", content = @Content)
    })
    @GetMapping("/aulas/{idAula}/inscricoes")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Page<InscricaoResponse>> buscarInscricoes(
            @Parameter(description = "ID da aula", required = true) @PathVariable UUID idAula,
            Pageable pageable) {
        var inscricoes = inscricaoService.buscarInscricoesPorAula(idAula, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(inscricoes);
    }

    @Operation(summary = "Minhas inscrições", description = "Retorna as inscrições do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de inscrições do usuário retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InscricaoResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    @GetMapping("/aulas/inscricoes/minhas")
    @PreAuthorize("hasAnyRole('ALUNO')")
    public ResponseEntity<Page<InscricaoResponse>> buscarInscricoesProprias(
            HttpServletRequest request,
            Pageable pageable) {
        var inscricoes = inscricaoService.buscarInscricoesProprias(request, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(inscricoes);
    }
}
