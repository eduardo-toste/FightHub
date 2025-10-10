package com.fighthub.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RecuperarSenhaRequest(

        @NotBlank(message = "E-mail é obrigatório")
        String email

) {
}
