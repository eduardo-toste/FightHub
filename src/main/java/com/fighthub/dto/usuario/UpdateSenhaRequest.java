package com.fighthub.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record UpdateSenhaRequest(

        @NotBlank(message = "Senha é obrigatória")
        String senha

) {
}
