package com.omnidrive.exception;

import java.io.Serial;

/**
 * Excepción lanzada cuando un vehículo usado no aprueba la revisión técnica requerida.
 */
public class TechnicalReviewRejectedException extends OmniDriveException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TechnicalReviewRejectedException(String message) {
        super(message);
    }
}
