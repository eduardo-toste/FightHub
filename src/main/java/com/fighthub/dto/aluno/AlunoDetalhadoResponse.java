package com.fighthub.dto.aluno;

import com.fighthub.dto.endereco.EnderecoResponse;
import com.fighthub.model.GraduacaoAluno;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AlunoDetalhadoResponse(
        UUID id,
        String nome,
        String email,
        String telefone,
        String foto,
        LocalDate dataNascimento,
        LocalDate dataMatricula,
        boolean matriculaAtiva,
        GraduacaoAluno graduacaoAluno,
        EnderecoResponse endereco,
        List<AlunoDetalhadoResponse.ResponsavelResumo> responsaveis
) {
    public record ResponsavelResumo(
            UUID id,
            String nome,
            String email
    ) {}
}
