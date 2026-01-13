package com.fighthub.utils.role.Responsavel;

import com.fighthub.model.Responsavel;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.ResponsavelRepository;
import com.fighthub.utils.role.RoleEnterHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResponsavelEnterHandler implements RoleEnterHandler {

    private final ResponsavelRepository responsavelRepository;

    @Override
    public Role getTargetRole() {
        return Role.RESPONSAVEL;
    }

    @Override
    public void onEnter(Usuario usuario) {
        if (responsavelRepository.existsByUsuarioId(usuario.getId())) return;
        var r = new Responsavel();
        r.setUsuario(usuario);
        responsavelRepository.save(r);
    }
}