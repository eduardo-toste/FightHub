package com.fighthub.utils.role.Professor;

import com.fighthub.model.enums.Role;
import com.fighthub.model.Usuario;
import com.fighthub.model.Professor;
import com.fighthub.repository.ProfessorRepository;
import com.fighthub.utils.role.RoleEnterHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfessorEnterHandler implements RoleEnterHandler {

    private final ProfessorRepository professorRepository;

    @Override
    public Role getTargetRole() {
        return Role.PROFESSOR;
    }

    @Override
    public void onEnter(Usuario usuario) {
        if (professorRepository.existsByUsuarioId(usuario.getId())) return;
        var p = new Professor();
        p.setUsuario(usuario);
        professorRepository.save(p);
    }
}