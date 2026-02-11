package com.fighthub.mapper;

import com.fighthub.dto.inscricao.InscricaoResponse;
import com.fighthub.model.Inscricao;
import org.springframework.data.domain.Page;

import java.util.List;

public class InscricaoMapper {

    public static InscricaoResponse toDTO(Inscricao inscricao) {
        var aula = inscricao.getAula();
        var turma = aula != null ? aula.getTurma() : null;
        
        return new InscricaoResponse(
                inscricao.getId(),
                inscricao.getAluno().getId(),
                aula != null ? aula.getId() : null,
                aula != null ? aula.getTitulo() : null,
                aula != null ? aula.getDescricao() : null,
                aula != null ? aula.getData() : null,
                turma != null ? turma.getNome() : null,
                aula != null ? aula.getLimiteAlunos() : 0,
                inscricao.getStatus(),
                inscricao.getInscritoEm()
        );
    }

    public static Page<InscricaoResponse> toPageDTO(Page<Inscricao> page) {
        return page.map(InscricaoMapper::toDTO);
    }

    public static List<InscricaoResponse> toListDTO(List<Inscricao> inscricoes) {
        return inscricoes.stream()
                .map(InscricaoMapper::toDTO)
                .toList();
    }

}
