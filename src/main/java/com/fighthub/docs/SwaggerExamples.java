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

    public static final String TOKEN_INVALIDO = """
        {
          "timestamp": "2025-09-09T17:49:11.68509-03:00",
          "status": 401,
          "error": "Unauthorized",
          "message": "Token inválido ou malformado.",
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

    public static final String ERRO_VALIDACAO = """
        {
          "timestamp": "2025-09-23T15:11:37.967005-03:00",
          "status": 400,
          "error": "Bad Request",
          "message": "Erro de validação: CPF inválido ou campos obrigatórios ausentes",
          "path": "/alunos"
        }
    """;

    public static final String ALUNO_NAO_ENCONTRADO = """
        {
          "timestamp": "2025-09-23T15:13:12.123456-03:00",
          "status": 404,
          "error": "Not Found",
          "message": "Aluno não encontrado",
          "path": "/alunos/{id}"
        }
    """;

    public static final String RESPONSAVEL_NAO_ENCONTRADO = """
        {
          "timestamp": "2025-09-23T15:13:12.123456-03:00",
          "status": 404,
          "error": "Not Found",
          "message": "Responsavel não encontrado",
          "path": "/responsavel/{id}"
        }
    """;

    public static final String USUARIO_NAO_ENCONTRADO = """
        {
          "timestamp": "2025-09-23T15:13:12.123456-03:00",
          "status": 404,
          "error": "Not Found",
          "message": "Usuario não encontrado",
          "path": "/usuarios/{id}"
        }
    """;

    public static final String STATUS_INVALIDO = """
        {
          "timestamp": "2025-09-23T15:13:12.123456-03:00",
          "status": 409,
          "error": "Conflict",
          "message": "Usuário já está ativo",
          "path": "/alunos/{id}/status"
        }
    """;

    public static final String ACESSO_NEGADO = """
        {
          "timestamp": "2025-09-23T15:13:12.123456-03:00",
          "status": 403,
          "error": "Forbidden",
          "message": "Acesso negado",
          "path": "/alunos"
        }
    """;

    public static final String MATRICULA_INVALIDA = """
        {
          "timestamp": "2025-09-23T15:13:12.123456-03:00",
          "status": 409,
          "error": "Conflict",
          "message": "A situação atual da matricula já está neste estado.",
          "path": "/alunos/{id}/matricula"
        }
    """;
}