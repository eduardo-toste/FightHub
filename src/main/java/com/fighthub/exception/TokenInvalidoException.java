package com.fighthub.exception;

public class TokenInvalidoException extends BusinessException {

    public TokenInvalidoException() {
        super("Token JWT inválido ou malformado.");
    }

}
