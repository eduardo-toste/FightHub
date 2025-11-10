package com.fighthub.controller;

import com.fighthub.dto.aula.AulaRequest;
import com.fighthub.dto.aula.AulaResponse;
import com.fighthub.dto.aula.AulaUpdateCompletoRequest;
import com.fighthub.dto.aula.AulaUpdateStatusRequest;
import com.fighthub.exception.dto.ErrorResponse;
import com.fighthub.service.AulaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/aulas")
@RequiredArgsConstructor
@Tag(name = "Aulas", description = "Endpoints para criação, atualização e gerenciamento de aulas no FightHub")
public class AulaController {

    private final AulaService aulaService;

    @Operation(
            summary = "Criação de nova aula",
            description = """
                    Permite que **ADMIN, COORDENADOR ou PROFESSOR** cadastrem uma nova aula no sistema.
                    
                    - A aula é criada inicialmente sem vínculo obrigatório com turma.
                    - Campos obrigatórios: título, data, limite de alunos.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Aula criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<AulaResponse> criarAula(@RequestBody @Valid AulaRequest request) {
        aulaService.criarAula(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Listagem de todas as aulas",
            description = "Retorna uma lista paginada de todas as aulas cadastradas."
    )
    @ApiResponse(responseCode = "200", description = "Lista de aulas retornada com sucesso")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Page<AulaResponse>> buscarAulas(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(aulaService.buscarAulas(pageable));
    }

    @Operation(
            summary = "Listagem de aulas disponíveis para o aluno",
            description = """
                    Retorna apenas as aulas cujas turmas estão associadas ao aluno autenticado.
                    
                    - Utiliza o token JWT para identificar o aluno.
                    """
    )
    @ApiResponse(responseCode = "200", description = "Aulas do aluno retornadas com sucesso")
    @GetMapping("/alunos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALUNO')")
    public ResponseEntity<Page<AulaResponse>> buscarAulasDisponiveisAluno(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(aulaService.buscarAulasDisponiveisAluno(pageable,request));
    }

    @Operation(
            summary = "Consulta de aula por ID",
            description = "Retorna os dados detalhados de uma aula específica."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aula encontrada"),
            @ApiResponse(responseCode = "404", description = "Aula não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AulaResponse> buscarAulaPorId(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(aulaService.buscarAulaPorId(id));
    }

    @Operation(
            summary = "Atualização do status de uma aula",
            description = """
                    Atualiza apenas o campo `status` da aula. 
                    
                    - Ex: Ativar/Inativar a aula.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Aula não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<AulaResponse> atualizarStatusAula(@PathVariable UUID id, @RequestBody @Valid AulaUpdateStatusRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(aulaService.atualizarStatus(id, request));
    }

    @Operation(
            summary = "Atualização completa da aula",
            description = "Atualiza todos os campos editáveis da aula, incluindo título, descrição, data, limite e status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aula atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Aula não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AulaResponse> atualizarAula(@RequestBody @Valid AulaUpdateCompletoRequest request, @PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(aulaService.atualizarAula(request, id));
    }

    @Operation(
            summary = "Vincular aula a turma",
            description = "Associa uma aula existente a uma turma específica."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma vinculada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Aula ou turma não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Turma já vinculada à aula",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{idAula}/turmas/{idTurma}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Void> vincularTurma(@PathVariable UUID idAula, @PathVariable UUID idTurma) {
        aulaService.vincularTurma(idAula, idTurma);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(
            summary = "Desvincular aula de turma",
            description = "Remove a associação entre uma aula e sua turma vinculada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma desvinculada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Aula ou turma não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{idAula}/turmas/{idTurma}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Void> desvincularTurma(@PathVariable UUID idAula, @PathVariable UUID idTurma) {
        aulaService.desvincularTurma(idAula, idTurma);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(
            summary = "Excluir aula",
            description = "Remove uma aula permanentemente do sistema."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aula excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Aula não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirAula(@PathVariable UUID id) {
        aulaService.excluirAula(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}