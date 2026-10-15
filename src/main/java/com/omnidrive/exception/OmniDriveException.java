package com.omnidrive.exception;

import java.io.Serial;

/**
 * Excepción base del dominio OmniDrive.
 * Todas las excepciones de reglas de negocio heredan de esta clase.
 */
public class OmniDriveException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public OmniDriveException(String message) {
        super(message);
    }

    public OmniDriveException(String message, Throwable cause) {
        super(message, cause);
    }
}
