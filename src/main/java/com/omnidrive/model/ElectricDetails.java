package com.omnidrive.model;

import com.omnidrive.exception.InvalidVehicleDataException;

import java.io.Serial;
import java.io.Serializable;

/**
 * Detalles específicos para vehículos con propulsión eléctrica.
 */
public class ElectricDetails implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final double autonomyKm;
    private final double averageChargingHours;

    public ElectricDetails(double autonomyKm, double averageChargingHours) throws InvalidVehicleDataException {
        if (autonomyKm <= 0) {
            throw new InvalidVehicleDataException("La autonomía del vehículo eléctrico debe ser mayor a 0 km.");
        }
        if (averageChargingHours <= 0) {
            throw new InvalidVehicleDataException("El tiempo promedio de recarga debe ser mayor a 0 horas.");
        }
        this.autonomyKm = autonomyKm;
        this.averageChargingHours = averageChargingHours;
    }

    public double getAutonomyKm() {
        return autonomyKm;
    }

    public double getAverageChargingHours() {
        return averageChargingHours;
    }
}
