package com.omnidrive.exception;

import java.io.Serial;

/**
 * Excepción lanzada cuando un empleado en estado bloqueado intenta autenticarse u operar.
 */
public class BlockedUserException extends AuthenticationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BlockedUserException(String message) {
        super(message);
    }

    public BlockedUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
