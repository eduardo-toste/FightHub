package com.fighthub.dto.aluno;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AlunoUpdateDataNascimentoRequest(

        @NotNull(message = "Data de nascimento é obrigatória, com o formato AAAA-MM-DD")
        LocalDate dataNascimento

) {
}
