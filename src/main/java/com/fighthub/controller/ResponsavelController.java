package com.fighthub.controller;

import com.fighthub.docs.SwaggerExamples;
import com.fighthub.dto.responsavel.CriarResponsavelRequest;
import com.fighthub.dto.responsavel.ResponsavelDetalhadoResponse;
import com.fighthub.dto.responsavel.ResponsavelResponse;
import com.fighthub.exception.dto.ErrorResponse;
import com.fighthub.service.ResponsavelService;
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
@RequestMapping("/responsaveis")
@RequiredArgsConstructor
@Tag(name = "Responsáveis", description = "Endpoints para criação e gerenciamento de Responsáveis no FightHub")
public class ResponsavelController {

    private final ResponsavelService responsavelService;

    @Operation(
            summary = "Criação de novo responsavel",
            description = """
                    Permite que **ADMIN, COORDENADOR ou PROFESSOR** cadastrem um novo responsavel no sistema.
                    
                    - Valida CPF, e-mail e data de nascimento.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Responsavel criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Erro de validação", value = SwaggerExamples.ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para acessar este recurso.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Acesso negado", value = SwaggerExamples.ACESSO_NEGADO)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Void> criarResponsavel(@RequestBody @Valid CriarResponsavelRequest request) {
        responsavelService.criacaoResponsavel(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Listagem de responsaveis",
            description = "Retorna uma lista paginada de responsaveis cadastrados."
    )
    @ApiResponse(responseCode = "200", description = "Lista de responsaveis retornada com sucesso",
            content = @Content(schema = @Schema(implementation = ResponsavelResponse.class)))
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<Page<ResponsavelResponse>> obterResponsaveis(Pageable pageable) {
        var responsaveis = responsavelService.obterTodosResponsaveis(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(responsaveis);
    }

    @Operation(
            summary = "Consulta de responsavel por ID",
            description = "Retorna os dados detalhados de um responsavel específico pelo seu ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Responsavel encontrado",
                    content = @Content(schema = @Schema(implementation = ResponsavelDetalhadoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Responsavel não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Responsavel não encontrado", value = SwaggerExamples.RESPONSAVEL_NAO_ENCONTRADO)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR', 'PROFESSOR')")
    public ResponseEntity<ResponsavelDetalhadoResponse> obterResponsavel(@PathVariable UUID id) {
        var responsavel = responsavelService.obterResponsavelPorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(responsavel);
    }

    @Operation(
            summary = "Vínculo de aluno",
            description = "Vincula o aluno ao responsável sem retornar nenhum conteúdo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno vinculado"),
            @ApiResponse(responseCode = "404", description = "Responsável ou aluno não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Responsável não encontrado", value = SwaggerExamples.RESPONSAVEL_NAO_ENCONTRADO),
                                    @ExampleObject(name = "Aluno não encontrado", value = SwaggerExamples.ALUNO_NAO_ENCONTRADO)
                            })),
            @ApiResponse(responseCode = "409", description = "Aluno já vinculado ao responsável",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Aluno já vinculado",
                                    value = SwaggerExamples.ALUNO_JA_VINCULADO)))
    })
    @PatchMapping("/{idResponsavel}/alunos/{idAluno}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR')")
    public ResponseEntity<Void> vincularAlunoAoResponsavel(@PathVariable UUID idResponsavel, @PathVariable UUID idAluno) {
        responsavelService.vincularAlunoAoResponsavel(idResponsavel, idAluno);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(
            summary = "Desnvínculo de aluno",
            description = "Desvincula o aluno ao responsável sem retornar nenhum conteúdo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno desvinculado"),
            @ApiResponse(responseCode = "404", description = "Responsável ou aluno não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Responsável não encontrado", value = SwaggerExamples.RESPONSAVEL_NAO_ENCONTRADO),
                                    @ExampleObject(name = "Aluno não encontrado", value = SwaggerExamples.ALUNO_NAO_ENCONTRADO)
                            })),
            @ApiResponse(responseCode = "409", description = "Responsável não vinculado ao aluno",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Responsável não vinculado ao aluno",
                                    value = SwaggerExamples.ALUNO_NAO_VINCULADO)))
    })
    @DeleteMapping("/{idResponsavel}/alunos/{idAluno}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR')")
    public ResponseEntity<Void> removerVinculoAlunoEResponsavel(@PathVariable UUID idResponsavel, @PathVariable UUID idAluno) {
        responsavelService.removerVinculoAlunoEResponsavel(idResponsavel, idAluno);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
