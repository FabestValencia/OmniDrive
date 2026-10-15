package com.omnidrive.exception;

import java.io.Serial;

/**
 * Excepción lanzada ante fallos en la autenticación de credenciales de usuario.
 */
public class AuthenticationException extends OmniDriveException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
