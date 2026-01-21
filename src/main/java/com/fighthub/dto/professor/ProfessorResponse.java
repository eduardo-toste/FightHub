package com.fighthub.dto.professor;

import java.util.UUID;

public record ProfessorResponse(

        UUID id,
        String nome,
        String email,
        String telefone,
        String cpf,
        String foto

) {
}
