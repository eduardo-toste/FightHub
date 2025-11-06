package com.fighthub.mapper;

import com.fighthub.dto.aula.AulaRequest;
import com.fighthub.dto.aula.AulaResponse;
import com.fighthub.model.Aula;
import com.fighthub.model.Turma;
import org.springframework.data.domain.Page;

public class AulaMapper {

    public static Aula toEntity(AulaRequest request, Turma turma) {
        return new Aula(
              request.titulo(),
              request.descricao(),
              request.data(),
              turma,
              request.limiteAlunos()
        );
    }

    public static AulaResponse toDTO(Aula aula) {
        return new AulaResponse(
                aula.getId(),
                aula.getTitulo(),
                aula.getDescricao(),
                aula.getData(),
                aula.getTurma().getId(),
                aula.getLimiteAlunos(),
                aula.isAtivo()
        );
    }

    public static Page<AulaResponse> toPageDTO(Page<Aula> page) {
        return page.map(AulaMapper::toDTO);
    }

}
