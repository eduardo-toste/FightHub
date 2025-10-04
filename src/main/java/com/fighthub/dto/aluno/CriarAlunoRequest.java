package com.fighthub.dto.aluno;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CriarAlunoRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail deve ser válido")
        String email,

        @CPF(message = "CPF deve ser válido")
        @NotBlank(message = "CPF é obrigatório")
        String cpf,

        @NotBlank(message = "Data de nascimento é obrigatória, com o formato AAAA-MM-DD")
        LocalDate dataNascimento,

        List<UUID> idsResponsaveis
) {
}
