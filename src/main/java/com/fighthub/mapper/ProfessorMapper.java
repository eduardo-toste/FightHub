package com.fighthub.mapper;

import com.fighthub.dto.endereco.EnderecoResponse;
import com.fighthub.dto.professor.ProfessorDetalhadoResponse;
import com.fighthub.dto.professor.ProfessorResponse;
import com.fighthub.model.Professor;
import org.springframework.data.domain.Page;

public class ProfessorMapper {

        public static ProfessorResponse toDTO(Professor professor) {
            return new ProfessorResponse(
                    professor.getId(),
                    professor.getUsuario().getNome(),
                    professor.getUsuario().getEmail(),
                    professor.getUsuario().getTelefone(),
                    professor.getUsuario().getCpf(),
                    professor.getUsuario().getFoto()
            );
        }

        public static ProfessorDetalhadoResponse toDetailedDTO(Professor professor) {
            return new ProfessorDetalhadoResponse(
                    professor.getId(),
                    professor.getUsuario().getNome(),
                    professor.getUsuario().getEmail(),
                    professor.getUsuario().getTelefone(),
                    professor.getUsuario().getCpf(),
                    professor.getUsuario().getFoto(),
                    EnderecoResponse.fromEntity(professor.getUsuario().getEndereco())
            );
        }

        public static Page<ProfessorResponse> toPageDTO(Page<Professor> page) {
            return page.map(ProfessorMapper::toDTO);
        }

}
