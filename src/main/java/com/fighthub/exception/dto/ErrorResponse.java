package com.fighthub.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String path,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<String> validationError
) {
    public static ErrorResponse from(HttpStatus status, String message, String path) {
        return new ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                null
        );
    }

    public static ErrorResponse from(HttpStatus status, String message, String path, List<String> validationError) {
        return new ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                validationError
        );
    }
}