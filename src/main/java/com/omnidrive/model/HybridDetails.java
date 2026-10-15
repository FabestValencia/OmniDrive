package com.omnidrive.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Detalles específicos para vehículos con propulsión híbrida.
 */
public class HybridDetails implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final boolean isPlugIn;
    private final boolean isMildHybrid;

    public HybridDetails(boolean isPlugIn, boolean isMildHybrid) {
        this.isPlugIn = isPlugIn;
        // Si es enchufable, por definición no es híbrido ligero (mild hybrid)
        this.isMildHybrid = !isPlugIn && isMildHybrid;
    }

    public boolean isPlugIn() {
        return isPlugIn;
    }

    public boolean isMildHybrid() {
        return isMildHybrid;
    }
}
