package com.omnidrive.model;

import com.omnidrive.exception.InvalidTransactionException;
import com.omnidrive.exception.VehicleUnavailableException;
import com.omnidrive.model.enums.TransactionType;
import com.omnidrive.model.enums.VehicleStatus;

import java.io.Serial;
import java.time.Instant;

/**
 * Representa una transacción de venta de un vehículo a un cliente.
 * Exige que el vehículo se encuentre en estado AVAILABLE_FOR_SALE.
 */
public class SaleTransaction extends Transaction {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Construye una transacción de venta.
     *
     * @param id          Identificador único de la transacción.
     * @param employee    Empleado responsable de la venta.
     * @param customer    Cliente comprador.
     * @param vehicle     Vehículo vendido (debe estar en AVAILABLE_FOR_SALE).
     * @param totalAmount Monto acordado de venta (positivo).
     * @param timestamp   Fecha y hora de la transacción.
     * @throws InvalidTransactionException si algún campo es nulo o inválido.
     * @throws VehicleUnavailableException si el vehículo no está disponible para venta.
     */
    public SaleTransaction(String id, Employee employee, Customer customer,
                           Vehicle vehicle, double totalAmount, Instant timestamp)
            throws InvalidTransactionException, VehicleUnavailableException {
        super(id, TransactionType.SALE, employee, customer, vehicle, totalAmount, timestamp);

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE_FOR_SALE) {
            throw new VehicleUnavailableException(
                    "El vehículo con placa " + vehicle.getPlate() +
                            " no está disponible para venta. Estado actual: " + vehicle.getStatus());
        }
    }
}
