package com.omnidrive.exception;

import java.io.Serial;

/**
 * Excepción lanzada cuando se intenta registrar una transacción con un identificador ya existente.
 */
public class DuplicateTransactionException extends OmniDriveException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DuplicateTransactionException(String message) {
        super(message);
    }
}
