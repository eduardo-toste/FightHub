package com.fighthub.dto.aluno;

import com.fighthub.dto.endereco.EnderecoRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AlunoUpdateParcialRequest(

        String nome,
        String email,
        String foto,
        String telefone,
        LocalDate dataNascimento,
        List<UUID> idsResponsaveis,
        EnderecoRequest endereco

) {
}
