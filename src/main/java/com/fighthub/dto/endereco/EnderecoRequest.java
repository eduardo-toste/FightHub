package com.fighthub.dto.endereco;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(

        @NotBlank(message = "CEP é obrigatório")
        @Size(max = 9)
        String cep,

        @NotBlank(message = "Logradouro é obrigatório")
        @Size(max = 100)
        String logradouro,

        @NotBlank(message = "Número é obrigatório")
        @Size(max = 10)
        String numero,

        @Size(max = 50)
        String complemento,

        @NotBlank(message = "Bairro é obrigatório")
        @Size(max = 50)
        String bairro,

        @NotBlank(message = "Cidade é obrigatório")
        @Size(max = 50)
        String cidade,

        @NotBlank(message = "Estado é obrigatório")
        @Size(min = 2, max = 2)
        String estado

) {
}