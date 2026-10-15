package com.omnidrive.model;

import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.TruckType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;

import java.io.Serial;

/**
 * Representa un vehículo de carga pesada tipo Camión.
 */
public class Truck extends Vehicle {

    @Serial
    private static final long serialVersionUID = 1L;

    private final double loadCapacityTons;
    private final boolean hasAirConditioning;
    private final boolean hasAirBrakes;
    private final boolean hasAbs;
    private final int axleCount;
    private final TruckType truckType;

    public Truck(String plate, String brand, int modelYear, int speedCount,
                 double maxSpeedKmH, double cylinderCapacityCc, FuelType fuelType,
                 TransmissionType transmissionType, VehicleCondition condition,
                 VehicleStatus status, double basePrice, String photoUrl,
                 boolean isTechnicalReviewApproved, ElectricDetails electricDetails,
                 HybridDetails hybridDetails, double loadCapacityTons,
                 boolean hasAirConditioning, boolean hasAirBrakes, boolean hasAbs,
                 int axleCount, TruckType truckType) throws InvalidVehicleDataException, TechnicalReviewRejectedException {
        super(plate, brand, modelYear, speedCount, maxSpeedKmH, cylinderCapacityCc,
                fuelType, transmissionType, condition, status, basePrice, photoUrl,
                isTechnicalReviewApproved, electricDetails, hybridDetails);

        if (loadCapacityTons <= 0) {
            throw new InvalidVehicleDataException("La capacidad de carga del camión debe ser mayor a cero toneladas.");
        }
        this.loadCapacityTons = loadCapacityTons;

        if (axleCount <= 0) {
            throw new InvalidVehicleDataException("El número de ejes del camión debe ser mayor a cero.");
        }
        this.axleCount = axleCount;

        if (truckType == null) {
            throw new InvalidVehicleDataException("El tipo de camión es obligatorio.");
        }
        this.truckType = truckType;

        this.hasAirConditioning = hasAirConditioning;
        this.hasAirBrakes = hasAirBrakes;
        this.hasAbs = hasAbs;
    }

    public double getLoadCapacityTons() {
        return loadCapacityTons;
    }

    public boolean hasAirConditioning() {
        return hasAirConditioning;
    }

    public boolean hasAirBrakes() {
        return hasAirBrakes;
    }

    public boolean hasAbs() {
        return hasAbs;
    }

    public int getAxleCount() {
        return axleCount;
    }

    public TruckType getTruckType() {
        return truckType;
    }
}
