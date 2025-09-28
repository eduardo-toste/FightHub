package com.fighthub.exception;

public class CpfExistenteException extends BusinessException {

    public CpfExistenteException() {
        super("Usuário já existente com este CPF");
    }

}
