package com.fighthub.controller;

import com.fighthub.docs.SwaggerExamples;
import com.fighthub.dto.aluno.*;
import com.fighthub.exception.dto.ErrorResponse;
import com.fighthub.service.AlunoService;
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
@RequestMapping("/alunos")
@RequiredArgsConstructor
@Tag(name = "Alunos", description = "Endpoints para criação e gerenciamento de Alunos no FightHub")
public class AlunoController {

    private final AlunoService alunoService;

    @Operation(
            summary = "Criação de novo aluno",
            description = """
                    Permite que **ADMIN, COORDENADOR ou PROFESSOR** cadastrem um novo aluno no sistema.
                    
                    - Valida CPF, e-mail e data de nascimento.
                    - Se informado, vincula os responsáveis ao aluno no momento do cadastro.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Aluno criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Erro de validação", value = SwaggerExamples.ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para acessar este recurso.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Acesso negado", value = SwaggerExamples.ACESSO_NEGADO)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Void> criarAluno(@RequestBody @Valid CriarAlunoRequest request) {
        alunoService.criarAluno(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Listagem de alunos",
            description = "Retorna uma lista paginada de alunos cadastrados."
    )
    @ApiResponse(responseCode = "200", description = "Lista de alunos retornada com sucesso",
            content = @Content(schema = @Schema(implementation = AlunoResponse.class)))
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Page<AlunoResponse>> obterAlunos(Pageable pageable) {
        Page<AlunoResponse> alunos = alunoService.obterTodos(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(alunos);
    }

    @Operation(
            summary = "Consulta de aluno por ID",
            description = "Retorna os dados detalhados de um aluno específico pelo seu ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno encontrado",
                    content = @Content(schema = @Schema(implementation = AlunoDetalhadoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Aluno não encontrado", value = SwaggerExamples.ALUNO_NAO_ENCONTRADO)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<AlunoDetalhadoResponse> obterAluno(@PathVariable UUID id) {
        var aluno = alunoService.obterAluno(id);
        return ResponseEntity.status(HttpStatus.OK).body(aluno);
    }

    @Operation(
            summary = "Atualização do Status de Matricula de Aluno",
            description = "Gerencia a situação da matricula do aluno, podendo estar ativa ou inativa."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status da Matricula atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Aluno não encontrado", value = SwaggerExamples.ALUNO_NAO_ENCONTRADO))),
            @ApiResponse(responseCode = "409", description = "Status de matricula já atualizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Status de matricula já atualizado", value = SwaggerExamples.MATRICULA_INVALIDA))),
    })
    @PatchMapping("/{id}/matricula")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR')")
    public ResponseEntity<Void> updateMatricula(@PathVariable UUID id, @RequestBody AlunoUpdateMatriculaRequest request) {
        alunoService.atualizarStatusMatricula(id, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}