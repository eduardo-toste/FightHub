package com.fighthub.dto.aluno;

import com.fighthub.dto.endereco.EnderecoResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AlunoDetalhadoResponse(
        UUID id,
        String nome,
        String email,
        String foto,
        boolean ativo,
        LocalDate dataNascimento,
        LocalDate dataMatricula,
        EnderecoResponse endereco,
        List<AlunoResponse.ResponsavelResumo> responsaveis
) {
    public record ResponsavelResumo(
            UUID id,
            String nome,
            String email
    ) {}
}
