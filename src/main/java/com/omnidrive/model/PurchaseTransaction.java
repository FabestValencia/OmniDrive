package com.omnidrive.model;

import com.omnidrive.exception.InvalidTransactionException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.model.enums.TransactionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;

import java.io.Serial;
import java.time.Instant;

/**
 * Representa una transacción de compra de vehículo a un cliente.
 * Exige revisión técnica aprobada para vehículos usados y define el estado inicial en inventario.
 */
public class PurchaseTransaction extends Transaction {

    @Serial
    private static final long serialVersionUID = 1L;

    private final VehicleStatus initialInventoryStatus;

    /**
     * Construye una transacción de compra.
     *
     * @param id                     Identificador único de la transacción.
     * @param employee               Empleado responsable de la compra.
     * @param customer               Cliente vendedor.
     * @param vehicle                Vehículo comprado.
     * @param totalAmount            Monto de compra acordado (positivo).
     * @param timestamp              Fecha y hora de la transacción.
     * @param initialInventoryStatus Estado asignado en inventario (AVAILABLE_FOR_SALE o AVAILABLE_FOR_RENT).
     * @throws InvalidTransactionException         si algún campo es nulo o inválido.
     * @throws TechnicalReviewRejectedException     si el vehículo es usado y no aprobó la revisión técnica.
     */
    public PurchaseTransaction(String id, Employee employee, Customer customer,
                               Vehicle vehicle, double totalAmount, Instant timestamp,
                               VehicleStatus initialInventoryStatus)
            throws InvalidTransactionException, TechnicalReviewRejectedException {
        super(id, TransactionType.PURCHASE, employee, customer, vehicle, totalAmount, timestamp);

        if (vehicle.getCondition() == VehicleCondition.USED && !vehicle.isTechnicalReviewApproved()) {
            throw new TechnicalReviewRejectedException(
                    "No se puede comprar el vehículo usado con placa " + vehicle.getPlate() +
                            " porque no ha aprobado la revisión técnica obligatoria");
        }

        if (initialInventoryStatus != VehicleStatus.AVAILABLE_FOR_SALE &&
                initialInventoryStatus != VehicleStatus.AVAILABLE_FOR_RENT) {
            throw new InvalidTransactionException(
                    "El estado inicial del vehículo comprado debe ser AVAILABLE_FOR_SALE o AVAILABLE_FOR_RENT, recibido: " +
                            initialInventoryStatus);
        }

        this.initialInventoryStatus = initialInventoryStatus;
    }

    public VehicleStatus getInitialInventoryStatus() {
        return initialInventoryStatus;
    }
}
