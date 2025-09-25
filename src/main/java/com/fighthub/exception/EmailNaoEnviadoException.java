package com.fighthub.exception;

public class EmailNaoEnviadoException extends BusinessException {

    public EmailNaoEnviadoException() {
        super("Erro ao enviar e-mail");
    }

}
