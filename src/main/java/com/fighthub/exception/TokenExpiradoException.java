package com.fighthub.exception;

public class TokenExpiradoException extends BusinessException {

    public TokenExpiradoException() {
        super("Token expirado.");
    }

}
