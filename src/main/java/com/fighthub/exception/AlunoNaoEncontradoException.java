package com.fighthub.exception;

public class AlunoNaoEncontradoException extends BusinessException {

    public AlunoNaoEncontradoException() {
        super("Aluno não encontrado.");
    }

}
