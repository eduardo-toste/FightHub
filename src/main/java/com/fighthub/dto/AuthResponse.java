package com.fighthub.dto;

public record AuthResponse(

        String accessToken,
        String refreshToken

) {
}
