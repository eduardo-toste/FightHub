package com.fighthub.dto.response;

public record AuthResponse(

        String accessToken,
        String refreshToken

) {
}
