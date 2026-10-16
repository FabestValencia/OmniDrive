package com.omnidrive.exception;

import java.io.Serial;

/**
 * Excepción lanzada cuando los límites temporales (fechas/horas) de un reporte son nulos o inconsistentes.
 */
public class InvalidReportDateRangeException extends OmniDriveException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidReportDateRangeException(String message) {
        super(message);
    }
}
