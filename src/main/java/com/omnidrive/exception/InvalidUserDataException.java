package com.omnidrive.exception;

import java.io.Serial;

/**
 * Excepción lanzada cuando los datos personales o credenciales no cumplen los formatos requeridos.
 */
public class InvalidUserDataException extends OmniDriveException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidUserDataException(String message) {
        super(message);
    }

    public InvalidUserDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
