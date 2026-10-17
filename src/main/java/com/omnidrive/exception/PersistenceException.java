package com.omnidrive.exception;

import java.io.Serial;

/**
 * Excepción lanzada cuando ocurre un error en la serialización, deserialización,
 * lectura o escritura de datos del concesionario en almacenamiento secundario.
 */
public class PersistenceException extends OmniDriveException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
