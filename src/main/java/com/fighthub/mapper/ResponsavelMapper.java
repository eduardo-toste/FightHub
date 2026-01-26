package com.fighthub.mapper;

import com.fighthub.dto.endereco.EnderecoResponse;
import com.fighthub.dto.responsavel.ResponsavelDetalhadoResponse;
import com.fighthub.dto.responsavel.ResponsavelResponse;
import com.fighthub.model.Responsavel;
import org.springframework.data.domain.Page;

public class ResponsavelMapper {

        public static ResponsavelResponse toDTO(Responsavel responsavel) {
            return new ResponsavelResponse(
                    responsavel.getId(),
                    responsavel.getUsuario().getNome(),
                    responsavel.getUsuario().getEmail(),
                    responsavel.getUsuario().getTelefone(),
                    responsavel.getUsuario().getCpf(),
                    responsavel.getUsuario().getFoto()
            );
        }

        public static ResponsavelDetalhadoResponse toDetailedDTO(Responsavel responsavel) {
            return new ResponsavelDetalhadoResponse(
                    responsavel.getId(),
                    responsavel.getUsuario().getNome(),
                    responsavel.getUsuario().getEmail(),
                    responsavel.getUsuario().getTelefone(),
                    responsavel.getUsuario().getCpf(),
                    responsavel.getUsuario().getFoto(),
                    EnderecoResponse.fromEntity(responsavel.getUsuario().getEndereco())
            );
        }

        public static Page<ResponsavelResponse> toPageDTO(Page<Responsavel> page) {
            return page.map(ResponsavelMapper::toDTO);
        }

}
