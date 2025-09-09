package com.fighthub.docs;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SwaggerExamples {

    public static final String TOKEN_MALFORMADO = """
        {
          "timestamp": "2025-09-09T17:49:11.68509-03:00",
          "status": 401,
          "error": "Unauthorized",
          "message": "Token JWT inválido ou malformado.",
          "path": "/auth/refresh"
        }
    """;

    public static final String TOKEN_EXPIRADO = """
        {
          "timestamp": "2025-09-09T17:49:27.511747-03:00",
          "status": 401,
          "error": "Unauthorized",
          "message": "Token expirado.",
          "path": "/auth/refresh"
        }
    """;

    public static final String CREDENCIAIS_INVALIDAS = """
        {
          "timestamp": "2025-09-09T17:49:11.68509-03:00",
          "status": 401,
          "error": "Unauthorized",
          "message": "Credenciais inválidas",
          "path": "/auth/login"
        }
    """;

}
