package com.omnidrive.model.enums;

import java.io.Serializable;

/**
 * Tipos de camión según su configuración de carrocería y carga.
 */
public enum TruckType implements Serializable {
    BOX_TRUCK,
    TANKER,
    HOPPER,
    FLATBED;

    private static final long serialVersionUID = 1L;
}
