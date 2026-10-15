package com.omnidrive.exception;

import java.io.Serial;

/**
 * Excepción lanzada al intentar registrar un vehículo con una placa ya existente.
 */
public class DuplicateVehicleException extends OmniDriveException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DuplicateVehicleException(String message) {
        super(message);
    }
}
