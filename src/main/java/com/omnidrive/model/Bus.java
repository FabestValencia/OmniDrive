package com.omnidrive.model;

import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;

import java.io.Serial;

/**
 * Representa un vehículo tipo Bus con salidas de emergencia y capacidad de ejes.
 */
public class Bus extends PassengerVehicle {

    @Serial
    private static final long serialVersionUID = 1L;

    private final double trunkCapacityLiters;
    private final int axleCount;
    private final int emergencyExitCount;

    public Bus(String plate, String brand, int modelYear, int speedCount,
               double maxSpeedKmH, double cylinderCapacityCc, FuelType fuelType,
               TransmissionType transmissionType, VehicleCondition condition,
               VehicleStatus status, double basePrice, String photoUrl,
               boolean isTechnicalReviewApproved, ElectricDetails electricDetails,
               HybridDetails hybridDetails, int passengerCount, int doorsCount,
               int airbagsCount, boolean hasAirConditioning, boolean hasReverseCamera,
               boolean hasAbs, double trunkCapacityLiters, int axleCount,
               int emergencyExitCount) throws InvalidVehicleDataException, TechnicalReviewRejectedException {
        super(plate, brand, modelYear, speedCount, maxSpeedKmH, cylinderCapacityCc,
                fuelType, transmissionType, condition, status, basePrice, photoUrl,
                isTechnicalReviewApproved, electricDetails, hybridDetails,
                passengerCount, doorsCount, airbagsCount, hasAirConditioning,
                hasReverseCamera, hasAbs);

        if (trunkCapacityLiters <= 0) {
            throw new InvalidVehicleDataException("La capacidad del maletero del Bus debe ser mayor a cero litros.");
        }
        this.trunkCapacityLiters = trunkCapacityLiters;

        if (axleCount <= 0) {
            throw new InvalidVehicleDataException("El número de ejes del Bus debe ser mayor a cero.");
        }
        this.axleCount = axleCount;

        if (emergencyExitCount <= 0) {
            throw new InvalidVehicleDataException("El número de salidas de emergencia del Bus debe ser mayor a cero.");
        }
        this.emergencyExitCount = emergencyExitCount;
    }

    public double getTrunkCapacityLiters() {
        return trunkCapacityLiters;
    }

    public int getAxleCount() {
        return axleCount;
    }

    public int getEmergencyExitCount() {
        return emergencyExitCount;
    }
}
