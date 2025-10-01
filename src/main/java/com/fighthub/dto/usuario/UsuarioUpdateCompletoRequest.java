package com.fighthub.dto.usuario;

import com.fighthub.dto.endereco.EnderecoRequest;
import com.fighthub.model.enums.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UsuarioUpdateCompletoRequest(

        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail deve ser válido")
        String email,

        String foto,

        @Pattern(
                regexp = "\\(?\\d{2}\\)?\\s?\\d{4,5}-\\d{4}",
                message = "Telefone deve estar no formato (XX)XXXXX-XXXX"
        )
        String telefone,

        @NotBlank(message = "CPF é obrigatório")
        @Pattern(
                regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11}",
                message = "CPF deve ser válido"
        )
        @CPF(message = "CPF deve ser válido")
        String cpf,

        @Valid
        @NotNull(message = "Endereço é obrigatório")
        EnderecoRequest endereco,

        @NotNull(message = "Role é obrigatória")
        Role role,

        @NotNull(message = "Status de ativação é obrigatório")
        Boolean ativo

) {
}
