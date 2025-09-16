package com.fighthub.dto;

import jakarta.validation.constraints.NotBlank;

public record AtivacaoRequest(

        @NotBlank
        String token,

        @NotBlank
        String senha

) {
}
