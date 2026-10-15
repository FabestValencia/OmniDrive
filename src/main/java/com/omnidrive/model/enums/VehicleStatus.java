package com.omnidrive.model.enums;

import java.io.Serializable;

/**
 * Estados controlados de disponibilidad del vehículo en el inventario.
 */
public enum VehicleStatus implements Serializable {
    AVAILABLE_FOR_SALE,
    AVAILABLE_FOR_RENT,
    SOLD,
    RENTED,
    MAINTENANCE;

    private static final long serialVersionUID = 1L;
}
