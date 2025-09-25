package com.fighthub.controller;

import com.fighthub.docs.SwaggerExamples;
import com.fighthub.dto.auth.AtivacaoRequest;
import com.fighthub.exception.dto.ErrorResponse;
import com.fighthub.service.AtivacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ativar")
@RequiredArgsConstructor
@Tag(name = "Ativação de Conta", description = "Endpoint para ativação de conta do usuário no FightHub")
public class AtivacaoController {

    private final AtivacaoService ativacaoService;

    @Operation(
            summary = "Ativação de conta",
            description = """
                    Ativa a conta de um usuário a partir de um token de ativação válido.
                    
                    - Requer token de ativação válido e não expirado.
                    - Permite definir senha, telefone e endereço no momento da ativação.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta ativada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Erro de validação", value = SwaggerExamples.ERRO_VALIDACAO))),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Token malformado", value = SwaggerExamples.TOKEN_MALFORMADO),
                                    @ExampleObject(name = "Token expirado", value = SwaggerExamples.TOKEN_EXPIRADO)
                            })),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado para o token informado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Aluno não encontrado", value = SwaggerExamples.ALUNO_NAO_ENCONTRADO)))
    })
    @PostMapping
    public ResponseEntity<Void> ativarConta(@RequestBody @Valid AtivacaoRequest request) {
        ativacaoService.ativarConta(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}