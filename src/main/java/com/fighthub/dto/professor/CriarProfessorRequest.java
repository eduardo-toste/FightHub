package com.fighthub.dto.professor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record CriarProfessorRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail deve ser válido")
        String email,

        @CPF(message = "CPF deve ser válido")
        @NotBlank(message = "CPF é obrigatório")
        String cpf

) {
}
