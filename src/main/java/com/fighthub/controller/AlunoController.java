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
    public ResponseEntity<AlunoDetalhadoResponse> obterAluno(@PathVariable UUID id) {
        var aluno = alunoService.obterAluno(id);
        return ResponseEntity.status(HttpStatus.OK).body(aluno);
    }

    @Operation(
            summary = "Atualização completa de aluno",
            description = """
                    Atualiza **todos os dados** de um aluno existente.
                    - Substitui completamente o estado atual.
                    - Exige endereço válido e todos os campos obrigatórios.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AlunoDetalhadoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Erro de validação", value = SwaggerExamples.ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Aluno não encontrado", value = SwaggerExamples.ALUNO_NAO_ENCONTRADO)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AlunoDetalhadoResponse> updateCompleto(
            @PathVariable UUID id,
            @RequestBody @Valid AlunoUpdateCompletoRequest request) {
        var alunoAtualizado = alunoService.updateAlunoCompleto(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(alunoAtualizado);
    }

    @Operation(
            summary = "Atualização parcial de aluno",
            description = "Permite atualizar apenas os campos informados do aluno, sem substituir o estado inteiro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AlunoDetalhadoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para atualização",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Erro de validação", value = SwaggerExamples.ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Aluno não encontrado", value = SwaggerExamples.ALUNO_NAO_ENCONTRADO)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<AlunoDetalhadoResponse> updateParcial(
            @PathVariable UUID id,
            @RequestBody @Valid AlunoUpdateParcialRequest request) {
        var alunoAtualizado = alunoService.updateAlunoParcial(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(alunoAtualizado);
    }

    @Operation(
            summary = "Desativação de aluno",
            description = "Desativa o aluno, impedindo sua participação em atividades até ser reativado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Aluno não encontrado", value = SwaggerExamples.ALUNO_NAO_ENCONTRADO)))
    })
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativarAluno(@PathVariable UUID id) {
        alunoService.desativarAluno(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(
            summary = "Reativação de aluno",
            description = "Reativa um aluno previamente desativado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno reativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Aluno não encontrado", value = SwaggerExamples.ALUNO_NAO_ENCONTRADO)))
    })
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativarAluno(@PathVariable UUID id) {
        alunoService.reativarAluno(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}