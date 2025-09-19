package com.fighthub.dto.auth;

public record AuthResponse(

        String accessToken,
        String refreshToken

) {
}
