package com.fighthub.dto.turma;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record TurmaRequest(

    @NotBlank(message = "Nome é obrigatório.")
    String nome,

    @NotBlank(message = "Horário é obrigatório.")
    String horario,

    UUID professorId

) {
}
