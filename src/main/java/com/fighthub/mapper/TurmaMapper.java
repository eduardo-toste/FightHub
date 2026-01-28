package com.fighthub.mapper;

import com.fighthub.dto.turma.TurmaRequest;
import com.fighthub.dto.turma.TurmaResponse;
import com.fighthub.model.Professor;
import com.fighthub.model.Turma;
import org.springframework.data.domain.Page;

public class TurmaMapper {

    public static TurmaResponse toDTO(Turma turma) {
        return new TurmaResponse(
                turma.getId(),
                turma.getNome(),
                turma.getHorario(),
                turma.getProfessor() != null ? turma.getProfessor().getId() : null,
                turma.isAtivo(),
                0
        );
    }

    public static TurmaResponse toDTO(Turma turma, long quantidadeAlunos) {
        return new TurmaResponse(
                turma.getId(),
                turma.getNome(),
                turma.getHorario(),
                turma.getProfessor() != null ? turma.getProfessor().getId() : null,
                turma.isAtivo(),
                (int) quantidadeAlunos
        );
    }

    public static Turma toEntity(TurmaRequest request, Professor professor) {
        return new Turma(
                request.nome(),
                request.horario(),
                professor
        );
    }

    public static Page<TurmaResponse> toPageDTO(Page<Turma> page) {
        return page.map(TurmaMapper::toDTO);
    }

}
