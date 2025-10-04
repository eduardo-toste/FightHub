package com.fighthub.dto.aluno;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record AlunoUpdateDataNascimentoRequest(

        @NotBlank(message = "Data de nascimento é obrigatória, com o formato AAAA-MM-DD")
        LocalDate dataNascimento

) {
}
