package com.fighthub.dto.responsavel;

import com.fighthub.dto.aluno.AlunoResponse;
import com.fighthub.dto.endereco.EnderecoResponse;

import java.util.List;
import java.util.UUID;

public record ResponsavelDetalhadoResponse(

        UUID id,
        String nome,
        String email,
        String telefone,
        String cpf,
        String foto,
        EnderecoResponse endereco,
        List<AlunoResponse> alunos

) {
}
