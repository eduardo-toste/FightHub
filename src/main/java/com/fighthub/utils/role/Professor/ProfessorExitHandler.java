package com.fighthub.utils.role.Professor;

import com.fighthub.model.enums.Role;
import com.fighthub.model.Usuario;
import com.fighthub.repository.ProfessorRepository;
import com.fighthub.utils.role.RoleExitHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfessorExitHandler implements RoleExitHandler {

    private final ProfessorRepository professorRepository;

    @Override
    public Role getSourceRole() {
        return Role.PROFESSOR;
    }

    @Override
    public void onExit(Usuario usuario) {
        professorRepository.findByUsuarioId(usuario.getId())
            .ifPresent(p -> professorRepository.deleteByUsuarioId(usuario.getId()));
    }
}