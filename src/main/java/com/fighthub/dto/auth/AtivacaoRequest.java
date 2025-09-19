package com.fighthub.dto.auth;

import com.fighthub.dto.endereco.EnderecoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AtivacaoRequest(

        @NotBlank
        String token,

        @NotBlank
        String senha,

        @Pattern(regexp = "\\(?\\d{2}\\)?\\s?\\d{4,5}-\\d{4}", message = "Telefone inválido")
        String telefone,

        @Valid
        EnderecoRequest endereco

) {
}
