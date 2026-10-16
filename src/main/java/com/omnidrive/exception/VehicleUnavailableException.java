package com.omnidrive.exception;

import java.io.Serial;

/**
 * Excepción lanzada cuando un vehículo no se encuentra en el estado requerido para una transacción.
 */
public class VehicleUnavailableException extends OmniDriveException {

    @Serial
    private static final long serialVersionUID = 1L;

    public VehicleUnavailableException(String message) {
        super(message);
    }
}
