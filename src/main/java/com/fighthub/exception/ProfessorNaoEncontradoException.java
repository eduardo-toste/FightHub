package com.fighthub.exception;

public class ProfessorNaoEncontradoException extends BusinessException {

    public ProfessorNaoEncontradoException() {
        super("Professor não encontrado.");
    }

}
