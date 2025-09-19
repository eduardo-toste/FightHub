package com.fighthub.dto.endereco;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(

        @NotBlank @Size(max = 9)
        String cep,

        @NotBlank @Size(max = 100)
        String logradouro,

        @NotBlank @Size(max = 10)
        String numero,

        @Size(max = 50)
        String complemento,

        @NotBlank @Size(max = 50)
        String bairro,

        @NotBlank @Size(max = 50)
        String cidade,

        @NotBlank @Size(min = 2, max = 2)
        String estado

) {
}