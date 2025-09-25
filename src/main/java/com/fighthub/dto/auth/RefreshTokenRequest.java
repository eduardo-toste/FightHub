package com.fighthub.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @NotBlank(message = "Refresh Token é obrigatório")
        String refreshToken

) {
}
