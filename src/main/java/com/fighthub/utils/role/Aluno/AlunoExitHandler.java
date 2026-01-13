package com.fighthub.utils.role.Aluno;

import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.utils.role.RoleExitHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlunoExitHandler implements RoleExitHandler {

    private final AlunoRepository alunoRepository;

    @Override
    public Role getSourceRole() {
        return Role.ALUNO;
    }

    @Override
    public void onExit(Usuario usuario) {
        alunoRepository.findByUsuarioId(usuario.getId())
                .ifPresent(a -> alunoRepository.deleteByUsuarioId(usuario.getId()));
    }

}
