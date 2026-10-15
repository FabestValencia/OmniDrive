package com.omnidrive.exception;

import java.io.Serial;

/**
 * Excepción lanzada cuando un código de recuperación de contraseña ha expirado o no coincide.
 */
public class InvalidRecoveryCodeException extends OmniDriveException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidRecoveryCodeException(String message) {
        super(message);
    }

    public InvalidRecoveryCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
