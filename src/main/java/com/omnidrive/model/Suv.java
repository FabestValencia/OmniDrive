package com.omnidrive.model;

import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;

import java.io.Serial;

/**
 * Representa una Camioneta / SUV con capacidad todoterreno y asistentes de seguridad.
 */
public class Suv extends PassengerVehicle {

    @Serial
    private static final long serialVersionUID = 1L;

    private final double trunkCapacityLiters;
    private final boolean hasCruiseControl;
    private final boolean hasCollisionSensors;
    private final boolean hasCrossTrafficAlert;
    private final boolean hasLaneKeepingAssist;
    private final boolean isFourByFour;

    public Suv(String plate, String brand, int modelYear, int speedCount,
               double maxSpeedKmH, double cylinderCapacityCc, FuelType fuelType,
               TransmissionType transmissionType, VehicleCondition condition,
               VehicleStatus status, double basePrice, String photoUrl,
               boolean isTechnicalReviewApproved, ElectricDetails electricDetails,
               HybridDetails hybridDetails, int passengerCount, int doorsCount,
               int airbagsCount, boolean hasAirConditioning, boolean hasReverseCamera,
               boolean hasAbs, double trunkCapacityLiters, boolean hasCruiseControl,
               boolean hasCollisionSensors, boolean hasCrossTrafficAlert,
               boolean hasLaneKeepingAssist, boolean isFourByFour)
            throws InvalidVehicleDataException, TechnicalReviewRejectedException {
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
        this.isFourByFour = isFourByFour;
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

    public boolean isFourByFour() {
        return isFourByFour;
    }
}
