package com.omnidrive.model;

import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;

import java.io.Serial;

/**
 * Representa un vehículo tipo Sedán con asistentes de conducción avanzados.
 */
public class Sedan extends PassengerVehicle {

    @Serial
    private static final long serialVersionUID = 1L;

    private final double trunkCapacityLiters;
    private final boolean hasCruiseControl;
    private final boolean hasCollisionSensors;
    private final boolean hasCrossTrafficAlert;
    private final boolean hasLaneKeepingAssist;

    public Sedan(String plate, String brand, int modelYear, int speedCount,
                 double maxSpeedKmH, double cylinderCapacityCc, FuelType fuelType,
                 TransmissionType transmissionType, VehicleCondition condition,
                 VehicleStatus status, double basePrice, String photoUrl,
                 boolean isTechnicalReviewApproved, ElectricDetails electricDetails,
                 HybridDetails hybridDetails, int passengerCount, int doorsCount,
                 int airbagsCount, boolean hasAirConditioning, boolean hasReverseCamera,
                 boolean hasAbs, double trunkCapacityLiters, boolean hasCruiseControl,
                 boolean hasCollisionSensors, boolean hasCrossTrafficAlert,
                 boolean hasLaneKeepingAssist) throws InvalidVehicleDataException, TechnicalReviewRejectedException {
        super(plate, brand, modelYear, speedCount, maxSpeedKmH, cylinderCapacityCc,
                fuelType, transmissionType, condition, status, basePrice, photoUrl,
                isTechnicalReviewApproved, electricDetails, hybridDetails,
                passengerCount, doorsCount, airbagsCount, hasAirConditioning,
                hasReverseCamera, hasAbs);

        if (trunkCapacityLiters <= 0) {
            throw new InvalidVehicleDataException("La capacidad del maletero debe ser mayor a cero litros.");
        }
        this.trunkCapacityLiters = trunkCapacityLiters;
        this.hasCruiseControl = hasCruiseControl;
        this.hasCollisionSensors = hasCollisionSensors;
        this.hasCrossTrafficAlert = hasCrossTrafficAlert;
        this.hasLaneKeepingAssist = hasLaneKeepingAssist;
    }

    public double getTrunkCapacityLiters() {
        return trunkCapacityLiters;
    }

    public boolean hasCruiseControl() {
        return hasCruiseControl;
    }

    public boolean hasCollisionSensors() {
        return hasCollisionSensors;
    }

    public boolean hasCrossTrafficAlert() {
        return hasCrossTrafficAlert;
    }

    public boolean hasLaneKeepingAssist() {
        return hasLaneKeepingAssist;
    }
}
