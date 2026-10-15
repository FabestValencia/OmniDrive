package com.omnidrive.model;

import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;

import java.io.Serial;

/**
 * Clase intermedia abstracta para vehículos diseñados para transporte de pasajeros.
 * Evita la duplicación de código en Sedan, Suv, Van y Bus.
 */
public abstract class PassengerVehicle extends Vehicle {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int passengerCount;
    private final int doorsCount;
    private final int airbagsCount;
    private final boolean hasAirConditioning;
    private final boolean hasReverseCamera;
    private final boolean hasAbs;

    public PassengerVehicle(String plate, String brand, int modelYear, int speedCount,
                            double maxSpeedKmH, double cylinderCapacityCc, FuelType fuelType,
                            TransmissionType transmissionType, VehicleCondition condition,
                            VehicleStatus status, double basePrice, String photoUrl,
                            boolean isTechnicalReviewApproved, ElectricDetails electricDetails,
                            HybridDetails hybridDetails, int passengerCount, int doorsCount,
                            int airbagsCount, boolean hasAirConditioning, boolean hasReverseCamera,
                            boolean hasAbs) throws InvalidVehicleDataException, TechnicalReviewRejectedException {
        super(plate, brand, modelYear, speedCount, maxSpeedKmH, cylinderCapacityCc,
                fuelType, transmissionType, condition, status, basePrice, photoUrl,
                isTechnicalReviewApproved, electricDetails, hybridDetails);

        if (passengerCount <= 0) {
            throw new InvalidVehicleDataException("El número de pasajeros debe ser mayor a cero.");
        }
        this.passengerCount = passengerCount;

        if (doorsCount <= 0) {
            throw new InvalidVehicleDataException("El número de puertas debe ser mayor a cero.");
        }
        this.doorsCount = doorsCount;

        if (airbagsCount < 0) {
            throw new InvalidVehicleDataException("El número de bolsas de aire no puede ser negativo.");
        }
        this.airbagsCount = airbagsCount;

        this.hasAirConditioning = hasAirConditioning;
        this.hasReverseCamera = hasReverseCamera;
        this.hasAbs = hasAbs;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public int getDoorsCount() {
        return doorsCount;
    }

    public int getAirbagsCount() {
        return airbagsCount;
    }

    public boolean hasAirConditioning() {
        return hasAirConditioning;
    }

    public boolean hasReverseCamera() {
        return hasReverseCamera;
    }

    public boolean hasAbs() {
        return hasAbs;
    }
}
