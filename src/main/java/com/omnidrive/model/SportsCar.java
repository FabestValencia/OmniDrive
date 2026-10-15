package com.omnidrive.model;

import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;

import java.io.Serial;

/**
 * Representa un vehículo deportivo de alto rendimiento en OmniDrive.
 */
public class SportsCar extends Vehicle {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int passengerCount;
    private final int doorsCount;
    private final int airbagsCount;
    private final int horsepower;
    private final double zeroToHundredSecs;

    public SportsCar(String plate, String brand, int modelYear, int speedCount,
                     double maxSpeedKmH, double cylinderCapacityCc, FuelType fuelType,
                     TransmissionType transmissionType, VehicleCondition condition,
                     VehicleStatus status, double basePrice, String photoUrl,
                     boolean isTechnicalReviewApproved, ElectricDetails electricDetails,
                     HybridDetails hybridDetails, int passengerCount, int doorsCount,
                     int airbagsCount, int horsepower, double zeroToHundredSecs)
            throws InvalidVehicleDataException, TechnicalReviewRejectedException {
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

        if (horsepower <= 0) {
            throw new InvalidVehicleDataException("El número de caballos de fuerza (HP) debe ser mayor a cero.");
        }
        this.horsepower = horsepower;

        if (zeroToHundredSecs <= 0) {
            throw new InvalidVehicleDataException("El tiempo de aceleración 0-100 km/h debe ser mayor a cero segundos.");
        }
        this.zeroToHundredSecs = zeroToHundredSecs;
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

    public int getHorsepower() {
        return horsepower;
    }

    public double getZeroToHundredSecs() {
        return zeroToHundredSecs;
    }
}
