package com.omnidrive.model.enums;

import java.io.Serializable;

/**
 * Tipos de combustible disponibles para los vehículos en OmniDrive.
 */
public enum FuelType implements Serializable {
    GASOLINE,
    DIESEL,
    ELECTRIC,
    HYBRID;

    private static final long serialVersionUID = 1L;
}
