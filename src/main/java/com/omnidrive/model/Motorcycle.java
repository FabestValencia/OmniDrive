package com.omnidrive.model;

import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;

import java.io.Serial;

/**
 * Representa una motocicleta en el catálogo de OmniDrive.
 */
public class Motorcycle extends Vehicle {

    @Serial
    private static final long serialVersionUID = 1L;

    public Motorcycle(String plate, String brand, int modelYear, int speedCount,
                      double maxSpeedKmH, double cylinderCapacityCc, FuelType fuelType,
                      TransmissionType transmissionType, VehicleCondition condition,
                      VehicleStatus status, double basePrice, String photoUrl,
                      boolean isTechnicalReviewApproved, ElectricDetails electricDetails,
                      HybridDetails hybridDetails) throws InvalidVehicleDataException, TechnicalReviewRejectedException {
        super(plate, brand, modelYear, speedCount, maxSpeedKmH, cylinderCapacityCc,
                fuelType, transmissionType, condition, status, basePrice, photoUrl,
                isTechnicalReviewApproved, electricDetails, hybridDetails);
    }
}
