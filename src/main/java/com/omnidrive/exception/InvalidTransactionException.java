package com.omnidrive.exception;

import java.io.Serial;

/**
 * Excepción lanzada cuando los datos de una transacción son inválidos o inconsistentes con las reglas de negocio.
 */
public class InvalidTransactionException extends OmniDriveException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidTransactionException(String message) {
        super(message);
    }
}
