package com.fighthub.dto.usuario;

import com.fighthub.model.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(

        @NotNull(message = "Status é obrigatório")
        boolean usuarioAtivo

) {
}
