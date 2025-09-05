package com.fighthub.exception;

public class UsuarioNaoEncontradoException extends BusinessException {

    public UsuarioNaoEncontradoException() {
        super("Usuário não encontrado.");
    }

}
