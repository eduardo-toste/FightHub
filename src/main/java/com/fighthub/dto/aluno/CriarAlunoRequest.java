package com.fighthub.dto.aluno;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CriarAlunoRequest(

        @NotBlank
        String nome,

        @NotBlank
        @Email
        String email,

        @CPF
        @NotBlank
        String cpf,

        @NotNull
        LocalDate dataNascimento,

        List<UUID> idsResponsaveis
) {
}
