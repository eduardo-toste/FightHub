package com.fighthub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CriarAlunoRequest(

        @NotBlank
        String nome,

        @NotBlank
        @Email
        String email,

        @NotNull
        LocalDate dataNascimento,

        List<UUID> idsResponsaveis
) {
}
