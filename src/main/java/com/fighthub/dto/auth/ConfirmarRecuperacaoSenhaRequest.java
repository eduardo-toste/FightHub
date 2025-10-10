package com.fighthub.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ConfirmarRecuperacaoSenhaRequest(

        @NotBlank(message = "Código de Recuperação é obrigatório")
        String codigoRecuperacao,

        @Email(message = "E-mail inválido")
        @NotBlank(message = "E-mail é obrigatório")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        String novaSenha

) {
}
