package com.fighthub.utils;

import com.fighthub.exception.dto.ErrorResponse;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@NoArgsConstructor
public class ErrorBuilder {

    public static ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String path) {
        ErrorResponse error = ErrorResponse.from(status, message, path);
        return ResponseEntity.status(status).body(error);
    }

}
