package com.fighthub.mapper;

import com.fighthub.dto.aluno.AlunoDetalhadoResponse;
import com.fighthub.dto.aluno.AlunoResponse;
import com.fighthub.dto.endereco.EnderecoResponse;
import com.fighthub.model.Aluno;
import org.springframework.data.domain.Page;

public class AlunoMapper {

    public static AlunoResponse toDTO(Aluno aluno) {
        return new AlunoResponse(
                aluno.getId(),
                aluno.getUsuario().getNome(),
                aluno.getUsuario().getEmail(),
                aluno.getUsuario().getFoto(),
                aluno.getUsuario().isAtivo(),
                aluno.getDataNascimento(),
                aluno.getDataMatricula()
        );
    }

    public static AlunoDetalhadoResponse toDetailedDTO(Aluno aluno) {
        return new AlunoDetalhadoResponse(
                aluno.getId(),
                aluno.getUsuario().getNome(),
                aluno.getUsuario().getEmail(),
                aluno.getUsuario().getFoto(),
                aluno.getUsuario().isAtivo(),
                aluno.getDataNascimento(),
                aluno.getDataMatricula(),
                EnderecoResponse.fromEntity(aluno.getUsuario().getEndereco()),
                aluno.getResponsaveis().stream()
                        .map(r -> new AlunoDetalhadoResponse.ResponsavelResumo(
                                r.getId(),
                                r.getUsuario().getNome(),
                                r.getUsuario().getEmail()
                        ))
                        .toList()
        );
    }

    public static Page<AlunoResponse> toPage(Page<Aluno> page) {
        return page.map(AlunoMapper::toDTO);
    }
}
