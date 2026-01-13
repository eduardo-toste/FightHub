package com.fighthub.utils.role.Aluno;

import com.fighthub.model.Aluno;
import com.fighthub.model.GraduacaoAluno;
import com.fighthub.model.Usuario;
import com.fighthub.model.enums.BeltGraduation;
import com.fighthub.model.enums.GraduationLevel;
import com.fighthub.model.enums.Role;
import com.fighthub.repository.AlunoRepository;
import com.fighthub.utils.role.RoleEnterHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AlunoEnterHandler implements RoleEnterHandler {

    private final AlunoRepository alunoRepository;

    @Override
    public Role getTargetRole() {
        return Role.ALUNO;
    }

    @Override
    public void onEnter(Usuario usuario) {
        if (alunoRepository.existsByUsuarioId(usuario.getId())) return;
        var aluno = new Aluno();
        aluno.setUsuario(usuario);

        // necessario ajustar
        aluno.setDataNascimento(LocalDate.now().minusYears(20));

        aluno.setDataMatricula(LocalDate.now());
        aluno.setMatriculaAtiva(true);
        aluno.setGraduacao(new GraduacaoAluno(BeltGraduation.BRANCA, GraduationLevel.ZERO));

        alunoRepository.save(aluno);
    }

}
