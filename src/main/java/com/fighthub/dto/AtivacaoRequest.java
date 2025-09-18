package com.fighthub.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CPF;

public record AtivacaoRequest(

        @NotBlank
        String token,

        @NotBlank
        String senha,

        @CPF
        @NotBlank
        String cpf,

        @Pattern(regexp = "\\(?\\d{2}\\)?\\s?\\d{4,5}-\\d{4}", message = "Telefone inválido")
        String telefone,

        @Valid
        EnderecoRequest endereco

) {
}
