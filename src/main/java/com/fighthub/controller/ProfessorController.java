package com.fighthub.controller;

import com.fighthub.docs.SwaggerExamples;
import com.fighthub.dto.professor.CriarProfessorRequest;
import com.fighthub.dto.professor.ProfessorDetalhadoResponse;
import com.fighthub.dto.professor.ProfessorResponse;
import com.fighthub.exception.dto.ErrorResponse;
import com.fighthub.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/professores")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorService professorService;

    @Operation(
            summary = "Criação de novo professor",
            description = """
                    Permite que **ADMIN, COORDENADOR** cadastrem um novo responsavel no sistema.
                    
                    - Valida CPF, e-mail e data de nascimento.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Professor criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Erro de validação", value = SwaggerExamples.ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "403", description = "Você não tem permissão para acessar este recurso.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Acesso negado", value = SwaggerExamples.ACESSO_NEGADO)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR')")
    public ResponseEntity<Void> criarProfessor(@RequestBody @Valid CriarProfessorRequest request) {
        professorService.criacaoProfessor(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Listagem de professores",
            description = "Retorna uma lista paginada de professores cadastrados."
    )
    @ApiResponse(responseCode = "200", description = "Lista de professores retornada com sucesso",
            content = @Content(schema = @Schema(implementation = ProfessorResponse.class)))
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR')")
    public ResponseEntity<Page<ProfessorResponse>> obterProfessores(Pageable pageable) {
        var professores = professorService.buscarProfessores(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(professores);
    }

    @Operation(
            summary = "Consulta de professor por ID",
            description = "Retorna os dados detalhados de um professor específico pelo seu ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Professor encontrado",
                    content = @Content(schema = @Schema(implementation = ProfessorDetalhadoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Professor não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Professor não encontrado", value = SwaggerExamples.RESPONSAVEL_NAO_ENCONTRADO)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR')")
    public ResponseEntity<ProfessorDetalhadoResponse> obterProfessor(@PathVariable UUID id) {
        var professor = professorService.buscarProfessorPorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(professor);
    }

}
