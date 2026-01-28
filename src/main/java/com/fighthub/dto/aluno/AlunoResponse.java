package com.fighthub.dto.aluno;

import com.fighthub.model.GraduacaoAluno;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AlunoResponse(
        UUID id,
        String nome,
        String email,
        String telefone,
        String foto,
        LocalDate dataNascimento,
        LocalDate dataMatricula,
        boolean matriculaAtiva,
        GraduacaoAluno graduacaoAluno,
        List<UUID> turmaIds
) {
}