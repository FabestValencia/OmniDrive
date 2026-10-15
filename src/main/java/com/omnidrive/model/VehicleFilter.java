package com.omnidrive.model;

import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;

import java.io.Serializable;

/**
 * Criterios inmutables para el filtrado y búsqueda de vehículos en el inventario.
 */
public class VehicleFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<? extends Vehicle> vehicleType;
    private final FuelType fuelType;
    private final VehicleCondition condition;
    private final Double minPrice;
    private final Double maxPrice;
    private final VehicleStatus status;

    private VehicleFilter(Builder builder) {
        this.vehicleType = builder.vehicleType;
        this.fuelType = builder.fuelType;
        this.condition = builder.condition;
        this.minPrice = builder.minPrice;
        this.maxPrice = builder.maxPrice;
        this.status = builder.status;
    }

    public Class<? extends Vehicle> getVehicleType() {
        return vehicleType;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public VehicleCondition getCondition() {
        return condition;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    /**
     * Evalúa si un vehículo satisface todos los criterios activos de este filtro.
     *
     * @param vehicle Vehículo a evaluar.
     * @return {@code true} si cumple con todos los filtros activos; {@code false} en caso contrario.
     */
    public boolean matches(Vehicle vehicle) {
        if (vehicle == null) {
            return false;
        }

        if (vehicleType != null && !vehicleType.isInstance(vehicle)) {
            return false;
        }

        if (fuelType != null && vehicle.getFuelType() != fuelType) {
            return false;
        }

        if (condition != null && vehicle.getCondition() != condition) {
            return false;
        }

        if (minPrice != null && vehicle.getBasePrice() < minPrice) {
            return false;
        }

        if (maxPrice != null && vehicle.getBasePrice() > maxPrice) {
            return false;
        }

        if (status != null && vehicle.getStatus() != status) {
            return false;
        }

        return true;
    }

    /**
     * Builder para construir instancias de {@link VehicleFilter}.
     */
    public static class Builder {
        private Class<? extends Vehicle> vehicleType;
        private FuelType fuelType;
        private VehicleCondition condition;
        private Double minPrice;
        private Double maxPrice;
        private VehicleStatus status;

        public Builder vehicleType(Class<? extends Vehicle> vehicleType) {
            this.vehicleType = vehicleType;
            return this;
        }

        public Builder fuelType(FuelType fuelType) {
            this.fuelType = fuelType;
            return this;
        }

        public Builder condition(VehicleCondition condition) {
            this.condition = condition;
            return this;
        }

        public Builder minPrice(Double minPrice) {
            this.minPrice = minPrice;
            return this;
        }

        public Builder maxPrice(Double maxPrice) {
            this.maxPrice = maxPrice;
            return this;
        }

        public Builder status(VehicleStatus status) {
            this.status = status;
            return this;
        }

        public VehicleFilter build() {
            return new VehicleFilter(this);
        }
    }
}
