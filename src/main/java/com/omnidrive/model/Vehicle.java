package com.omnidrive.model;

import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.Year;

/**
 * Clase base abstracta para la jerarquía de vehículos en OmniDrive.
 * Es completamente agnóstica a la interfaz gráfica y serializable.
 */
public abstract class Vehicle implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_PHOTO_URL = "/com/omnidrive/images/default-vehicle.png";

    private final String plate;
    private final String brand;
    private final int modelYear;
    private final int speedCount;
    private final double maxSpeedKmH;
    private final double cylinderCapacityCc;
    private final FuelType fuelType;
    private final TransmissionType transmissionType;
    private final VehicleCondition condition;
    private VehicleStatus status;
    private final double basePrice;
    private String photoUrl;
    private final boolean isTechnicalReviewApproved;
    private final ElectricDetails electricDetails;
    private final HybridDetails hybridDetails;

    public Vehicle(String plate, String brand, int modelYear, int speedCount,
                   double maxSpeedKmH, double cylinderCapacityCc, FuelType fuelType,
                   TransmissionType transmissionType, VehicleCondition condition,
                   VehicleStatus status, double basePrice, String photoUrl,
                   boolean isTechnicalReviewApproved, ElectricDetails electricDetails,
                   HybridDetails hybridDetails) throws InvalidVehicleDataException, TechnicalReviewRejectedException {

        // Validación de placa
        if (plate == null || plate.trim().isEmpty()) {
            throw new InvalidVehicleDataException("La placa del vehículo es obligatoria y no puede estar vacía.");
        }
        this.plate = plate.trim().toUpperCase();

        // Validación de marca
        if (brand == null || brand.trim().isEmpty()) {
            throw new InvalidVehicleDataException("La marca del vehículo no puede estar vacía.");
        }
        this.brand = brand.trim();

        // Validación de año modelo
        int currentYear = Year.now().getValue();
        if (modelYear < 1900 || modelYear > currentYear + 2) {
            throw new InvalidVehicleDataException("El año modelo (" + modelYear + ") es inválido.");
        }
        this.modelYear = modelYear;

        // Validaciones numéricas básicas
        if (speedCount <= 0) {
            throw new InvalidVehicleDataException("El número de cambios/velocidades debe ser mayor a cero.");
        }
        this.speedCount = speedCount;

        if (maxSpeedKmH <= 0) {
            throw new InvalidVehicleDataException("La velocidad máxima debe ser mayor a 0 km/h.");
        }
        this.maxSpeedKmH = maxSpeedKmH;

        if (cylinderCapacityCc < 0) {
            throw new InvalidVehicleDataException("El cilindraje no puede ser negativo.");
        }
        this.cylinderCapacityCc = cylinderCapacityCc;

        if (basePrice <= 0) {
            throw new InvalidVehicleDataException("El valor base económico (precio o tarifa) debe ser mayor a cero.");
        }
        this.basePrice = basePrice;

        // Validación de enums obligatorios
        if (fuelType == null || transmissionType == null || condition == null || status == null) {
            throw new InvalidVehicleDataException("El combustible, transmisión, condición y estado son obligatorios.");
        }
        this.fuelType = fuelType;
        this.transmissionType = transmissionType;
        this.condition = condition;
        this.status = status;

        // Regla crítica de negocio: Revisión técnica en vehículos usados
        if (condition == VehicleCondition.USED && !isTechnicalReviewApproved) {
            throw new TechnicalReviewRejectedException(
                    "No se puede registrar el vehículo usado con placa " + this.plate + " porque no tiene la revisión técnica aprobada."
            );
        }
        this.isTechnicalReviewApproved = isTechnicalReviewApproved;

        // Foto por defecto
        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            this.photoUrl = DEFAULT_PHOTO_URL;
        } else {
            this.photoUrl = photoUrl.trim();
        }

        // Validación de combustibles transversales
        if (fuelType == FuelType.ELECTRIC) {
            if (electricDetails == null) {
                throw new InvalidVehicleDataException("Los vehículos eléctricos requieren especificar autonomía y tiempo de recarga.");
            }
            this.electricDetails = electricDetails;
            this.hybridDetails = null;
        } else if (fuelType == FuelType.HYBRID) {
            if (hybridDetails == null) {
                throw new InvalidVehicleDataException("Los vehículos híbridos requieren especificar si son enchufables o ligeros.");
            }
            this.hybridDetails = hybridDetails;
            this.electricDetails = null;
        } else {
            this.electricDetails = null;
            this.hybridDetails = null;
        }
    }

    public String getPlate() {
        return plate;
    }

    public String getBrand() {
        return brand;
    }

    public int getModelYear() {
        return modelYear;
    }

    public int getSpeedCount() {
        return speedCount;
    }

    public double getMaxSpeedKmH() {
        return maxSpeedKmH;
    }

    public double getCylinderCapacityCc() {
        return cylinderCapacityCc;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public TransmissionType getTransmissionType() {
        return transmissionType;
    }

    public VehicleCondition getCondition() {
        return condition;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo.");
        }
        this.status = status;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            this.photoUrl = DEFAULT_PHOTO_URL;
        } else {
            this.photoUrl = photoUrl.trim();
        }
    }

    public boolean isTechnicalReviewApproved() {
        return isTechnicalReviewApproved;
    }

    public ElectricDetails getElectricDetails() {
        return electricDetails;
    }

    public HybridDetails getHybridDetails() {
        return hybridDetails;
    }
}
