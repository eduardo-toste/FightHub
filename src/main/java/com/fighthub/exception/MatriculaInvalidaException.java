package com.fighthub.exception;

public class MatriculaInvalidaException extends BusinessException {

    public MatriculaInvalidaException() {
        super("A situação atual da matricula já está neste estado.");
    }

}
