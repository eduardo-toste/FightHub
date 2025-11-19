package com.fighthub.mapper;

import com.fighthub.dto.presenca.PresencaResponse;
import com.fighthub.model.Presenca;
import org.springframework.data.domain.Page;

public class PresencaMapper {

    public static PresencaResponse toDTO(Presenca presenca) {
        return new PresencaResponse(
                presenca.getId(),
                presenca.isPresente(),
                presenca.getInscricao().getId(),
                presenca.getDataRegistro()
        );
    }

    public static Page<PresencaResponse> toPageDTO(Page<Presenca> presencas) {
        return presencas.map(PresencaMapper::toDTO);
    }

}
