package com.fighthub.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ValidarCodigoRecuperacaoRequest(

        @NotBlank(message = "Código de Recuperação é obrigatório")
        String codigoRecuperacao,

        @NotBlank(message = "E-mail é obrigatório")
        String email

) {
}
