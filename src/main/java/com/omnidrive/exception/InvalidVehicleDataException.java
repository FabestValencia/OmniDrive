package com.omnidrive.exception;

import java.io.Serial;

/**
 * Excepción lanzada cuando los datos de registro de un vehículo son inconsistentes o inválidos.
 */
public class InvalidVehicleDataException extends OmniDriveException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidVehicleDataException(String message) {
        super(message);
    }
}
