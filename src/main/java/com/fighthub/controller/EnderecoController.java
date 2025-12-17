package com.fighthub.controller;

import com.fighthub.integration.cep.dto.EnderecoPorCepResponse;
import com.fighthub.service.EnderecoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enderecos")
@RequiredArgsConstructor
@Tag(name = "Endereço", description = "Endpoint para consulta de endereços por CEP")
public class EnderecoController {

    private final EnderecoService enderecoService;

    @Operation(
            summary = "Buscar endereço por CEP",
            description = "Consulta os dados de endereço a partir do CEP utilizando o serviço de CEP integrado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Endereço retornado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EnderecoPorCepResponse.class))),
            @ApiResponse(responseCode = "400", description = "CEP inválido ou formato incorreto"),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado para o CEP informado"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao consultar serviço de CEP")
    })
    @GetMapping("/cep/{cep}")
    public ResponseEntity<EnderecoPorCepResponse> buscarEnderecoPorCep(@PathVariable String cep) {
        EnderecoPorCepResponse enderecoPorCepResponse = enderecoService.buscarEnderecoPorCep(cep);
        return ResponseEntity.ok(enderecoPorCepResponse);
    }


}
