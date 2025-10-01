package com.fighthub.dto.usuario;

import com.fighthub.model.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(

        @NotNull(message = "Role é obrigatória")
        Role role

) {
}
