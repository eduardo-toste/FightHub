package com.fighthub.controller;

import com.fighthub.docs.SwaggerExamples;
import com.fighthub.dto.turma.TurmaRequest;
import com.fighthub.dto.turma.TurmaResponse;
import com.fighthub.dto.turma.TurmaUpdateCompletoRequest;
import com.fighthub.dto.turma.TurmaUpdateStatusRequest;
import com.fighthub.exception.dto.ErrorResponse;
import com.fighthub.service.TurmaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/turmas")
@RequiredArgsConstructor
@Tag(name = "Turmas", description = "Endpoints para gerenciamento de turmas no sistema FightHub")
public class TurmaController {

    private final TurmaService turmaService;

    @Operation(summary = "Criação de nova turma", description = "Permite criar uma nova turma no sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turma criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Erro de validação", value = SwaggerExamples.ERRO_VALIDACAO)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COORDENADOR','PROFESSOR')")
    public ResponseEntity<Void> criarTurma(@RequestBody @Valid TurmaRequest request) {
        turmaService.criarTurma(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Listagem de turmas", description = "Retorna uma lista paginada de turmas cadastradas.")
    @ApiResponse(responseCode = "200", description = "Lista de turmas retornada com sucesso",
            content = @Content(schema = @Schema(implementation = TurmaResponse.class)))
    @GetMapping
    public ResponseEntity<Page<TurmaResponse>> buscarTurmas(Pageable pageable) {
        var turmas = turmaService.buscarTurmas(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(turmas);
    }

    @Operation(summary = "Busca de turma por ID", description = "Retorna os dados de uma turma específica pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma encontrada com sucesso",
                    content = @Content(schema = @Schema(implementation = TurmaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Turma não encontrada", value = SwaggerExamples.TURMA_NAO_ENCONTRADA)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TurmaResponse> buscarTurmaPorId(@PathVariable UUID id) {
        var turma = turmaService.buscarTurmaPorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(turma);
    }

    @Operation(summary = "Atualização completa de turma", description = "Atualiza todos os dados de uma turma existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = TurmaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Erro de validação", value = SwaggerExamples.ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Turma não encontrada", value = SwaggerExamples.TURMA_NAO_ENCONTRADA)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDENADOR','PROFESSOR')")
    public ResponseEntity<TurmaResponse> atualizarTurma(@PathVariable UUID id, @RequestBody @Valid TurmaUpdateCompletoRequest request) {
        var turmaAtualizada = turmaService.atualizarTurma(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(turmaAtualizada);
    }

    @Operation(summary = "Atualização de status da turma", description = "Ativa ou desativa uma turma específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = TurmaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Erro de validação", value = SwaggerExamples.ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Turma não encontrada", value = SwaggerExamples.TURMA_NAO_ENCONTRADA)))
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','COORDENADOR','PROFESSOR')")
    public ResponseEntity<TurmaResponse> atualizarStatusTurma(@PathVariable UUID id, @RequestBody @Valid TurmaUpdateStatusRequest request) {
        var turmaAtualizada = turmaService.atualizarStatusTurma(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(turmaAtualizada);
    }

    @Operation(summary = "Exclusão de turma", description = "Realiza a exclusão lógica ou soft delete de uma turma.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Turma não encontrada", value = SwaggerExamples.TURMA_NAO_ENCONTRADA)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDENADOR','PROFESSOR')")
    public ResponseEntity<Void> excluirTurma(@PathVariable UUID id) {
        turmaService.excluirTurma(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(
            summary = "Vínculo de professor",
            description = "Vincula o professor à turma sem retornar nenhum conteúdo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Professor vinculado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Turma ou professor não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Turma não encontrada", value = SwaggerExamples.TURMA_NAO_ENCONTRADA),
                                    @ExampleObject(name = "Professor não encontrado", value = SwaggerExamples.PROFESSOR_NAO_ENCONTRADO)
                            })),
            @ApiResponse(responseCode = "409", description = "Professor já vinculado à turma.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Professor já vinculado",
                                    value = SwaggerExamples.PROFESSOR_NAO_ENCONTRADO)))
    })
    @PatchMapping("/{idTurma}/professores/{idProfessor}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDENADOR')")
    public ResponseEntity<Void> vincularProfessor(@PathVariable UUID idTurma, @PathVariable UUID idProfessor) {
        turmaService.vincularProfessor(idTurma, idProfessor);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(
            summary = "Desvínculo de professor",
            description = "Desvincula o professor da turma sem retornar nenhum conteúdo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Professor desvinculado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Turma ou professor não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Turma não encontrada", value = SwaggerExamples.TURMA_NAO_ENCONTRADA),
                                    @ExampleObject(name = "Professor não encontrado", value = SwaggerExamples.PROFESSOR_NAO_ENCONTRADO)
                            })),
            @ApiResponse(responseCode = "409", description = "Professor não está vinculado à turma",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Professor já vinculado",
                                    value = SwaggerExamples.PROFESSOR_NAO_ENCONTRADO)))
    })
    @DeleteMapping("/{idTurma}/professores/{idProfessor}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDENADOR')")
    public ResponseEntity<Void> desvincularProfessor(@PathVariable UUID idTurma, @PathVariable UUID idProfessor) {
        turmaService.desvincularProfessor(idTurma, idProfessor);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(
            summary = "Vínculo de aluno",
            description = "Vincula o aluno á turma sem retornar nenhum conteúdo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno vinculado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Turma ou aluno não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Turma não encontrada", value = SwaggerExamples.TURMA_NAO_ENCONTRADA),
                                    @ExampleObject(name = "Aluno não encontrado", value = SwaggerExamples.ALUNO_NAO_ENCONTRADO)
                            })),
            @ApiResponse(responseCode = "409", description = "Aluno já vinculado à turma.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Aluno já vinculado",
                                    value = SwaggerExamples.ALUNO_JA_VINCULADO)))
    })
    @PatchMapping("/{idTurma}/alunos/{idAluno}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDENADOR')")
    public ResponseEntity<Void> vincularAluno(@PathVariable UUID idTurma, @PathVariable UUID idAluno) {
        turmaService.vincularAluno(idTurma, idAluno);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(
            summary = "Desvínculo de aluno",
            description = "Desvincula o aluno da turma sem retornar nenhum conteúdo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno desvinculado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Responsável ou aluno não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Turma não encontrada", value = SwaggerExamples.TURMA_NAO_ENCONTRADA),
                                    @ExampleObject(name = "Aluno não encontrado", value = SwaggerExamples.ALUNO_NAO_ENCONTRADO)
                            })),
            @ApiResponse(responseCode = "409", description = "Aluno não está vinculado à turma",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Aluno não está vinculado à turma.",
                                    value = SwaggerExamples.ALUNO_NAO_VINCULADO)))
    })
    @DeleteMapping("/{idTurma}/alunos/{idAluno}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDENADOR')")
    public ResponseEntity<Void> desvincularAluno(@PathVariable UUID idTurma, @PathVariable UUID idAluno) {
        turmaService.desvincularAluno(idTurma, idAluno);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}