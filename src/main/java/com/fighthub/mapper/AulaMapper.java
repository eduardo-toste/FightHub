package com.fighthub.mapper;

import com.fighthub.dto.aula.AulaRequest;
import com.fighthub.model.Aula;
import com.fighthub.model.Turma;

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

}
