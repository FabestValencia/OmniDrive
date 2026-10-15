package com.omnidrive.exception;

import java.io.Serial;

/**
 * Excepción lanzada cuando se intenta registrar una persona o usuario con cédula o nombre de usuario duplicado.
 */
public class DuplicateUserException extends OmniDriveException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String identifier;

    public DuplicateUserException(String identifier, String message) {
        super(message + " (Identificador en conflicto: " + identifier + ")");
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
