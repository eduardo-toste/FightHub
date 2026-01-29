package com.fighthub.mapper;

import com.fighthub.dto.inscricao.InscricaoResponse;
import com.fighthub.model.Inscricao;
import org.springframework.data.domain.Page;

import java.util.List;

public class InscricaoMapper {

    public static InscricaoResponse toDTO(Inscricao inscricao) {
        return new InscricaoResponse(
                inscricao.getId(),
                inscricao.getAluno().getId(),
                inscricao.getAula().getId(),
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
