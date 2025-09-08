package com.fighthub.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fighthub.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ErrorWriter {

    private final ObjectMapper mapper;

    public ErrorWriter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void write(HttpServletResponse response, HttpStatus status, String message, String path) throws IOException {
        if (response.isCommitted()) return;

        var body = ErrorResponse.from(status, message, path);
        response.resetBuffer();
        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(mapper.writeValueAsString(body));
        response.flushBuffer();
    }
}