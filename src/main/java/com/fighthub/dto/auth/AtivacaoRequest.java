package com.fighthub.dto.auth;

import com.fighthub.dto.endereco.EnderecoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AtivacaoRequest(

        @NotBlank(message = "Token é obrigatório")
        String token,

        @NotBlank(message = "Senha é obrigatória")
        String senha,

        @Pattern(
                regexp = "\\(?\\d{2}\\)?\\s?\\d{4,5}-\\d{4}",
                message = "Telefone deve estar no formato (XX)XXXXX-XXXX"
        )
        String telefone,

        @Valid
        EnderecoRequest endereco

) {
}
