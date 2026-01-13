package com.fighthub.utils.role.Responsavel;

import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.ResponsavelRepository;
import com.fighthub.utils.role.RoleExitHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResponsavelExitHandler implements RoleExitHandler {

    private final ResponsavelRepository responsavelRepository;

    @Override
    public Role getSourceRole() {
        return Role.RESPONSAVEL;
    }

    @Override
    public void onExit(Usuario usuario) {
        responsavelRepository.findByUsuarioId(usuario.getId())
            .ifPresent(r -> responsavelRepository.deleteByUsuarioId(usuario.getId()));
    }
}