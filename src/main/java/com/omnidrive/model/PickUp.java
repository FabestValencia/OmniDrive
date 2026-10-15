package com.omnidrive.model;

import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;

import java.io.Serial;

/**
 * Representa un vehículo utilitario tipo Pick Up con caja de carga y tracción 4x4.
 */
public class PickUp extends PassengerVehicle {

    @Serial
    private static final long serialVersionUID = 1L;

    private final boolean isFourByFour;
    private final double cargoBedCapacityKg;

    public PickUp(String plate, String brand, int modelYear, int speedCount,
                  double maxSpeedKmH, double cylinderCapacityCc, FuelType fuelType,
                  TransmissionType transmissionType, VehicleCondition condition,
                  VehicleStatus status, double basePrice, String photoUrl,
                  boolean isTechnicalReviewApproved, ElectricDetails electricDetails,
                  HybridDetails hybridDetails, int passengerCount, int doorsCount,
                  int airbagsCount, boolean hasAirConditioning, boolean hasReverseCamera,
                  boolean hasAbs, boolean isFourByFour, double cargoBedCapacityKg)
            throws InvalidVehicleDataException, TechnicalReviewRejectedException {
        super(plate, brand, modelYear, speedCount, maxSpeedKmH, cylinderCapacityCc,
                fuelType, transmissionType, condition, status, basePrice, photoUrl,
                isTechnicalReviewApproved, electricDetails, hybridDetails,
                passengerCount, doorsCount, airbagsCount, hasAirConditioning,
                hasReverseCamera, hasAbs);

        if (cargoBedCapacityKg <= 0) {
            throw new InvalidVehicleDataException("La capacidad de la caja de carga debe ser mayor a cero kg.");
        }
        this.isFourByFour = isFourByFour;
        this.cargoBedCapacityKg = cargoBedCapacityKg;
    }

    public boolean isFourByFour() {
        return isFourByFour;
    }

    public double getCargoBedCapacityKg() {
        return cargoBedCapacityKg;
    }
}
