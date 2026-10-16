package com.omnidrive.model;

import com.omnidrive.exception.InvalidTransactionException;
import com.omnidrive.exception.VehicleUnavailableException;
import com.omnidrive.model.enums.TransactionType;
import com.omnidrive.model.enums.VehicleStatus;

import java.io.Serial;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Representa una transacción de alquiler de vehículo a un cliente.
 * Exige que el vehículo esté en estado AVAILABLE_FOR_RENT, fechas consistentes
 * y calcula el monto total como los días de alquiler multiplicados por la tarifa base del vehículo.
 */
public class RentalTransaction extends Transaction {

    @Serial
    private static final long serialVersionUID = 1L;

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int rentalDays;

    /**
     * Construye una transacción de alquiler con cálculo automático de tarifa total.
     *
     * @param id         Identificador único de la transacción.
     * @param employee   Empleado responsable del alquiler.
     * @param customer   Cliente arrendatario.
     * @param vehicle    Vehículo arrendado (debe estar en AVAILABLE_FOR_RENT).
     * @param timestamp  Fecha y hora del registro de la transacción.
     * @param startDate  Fecha de inicio del alquiler.
     * @param endDate    Fecha de finalización del alquiler.
     * @param rentalDays Número de días de duración del alquiler (debe ser mayor a 0).
     * @throws InvalidTransactionException si las fechas son inválidas o los días no son positivos.
     * @throws VehicleUnavailableException si el vehículo no está disponible para alquiler.
     */
    public RentalTransaction(String id, Employee employee, Customer customer,
                             Vehicle vehicle, Instant timestamp,
                             LocalDate startDate, LocalDate endDate, int rentalDays)
            throws InvalidTransactionException, VehicleUnavailableException {
        super(id, TransactionType.RENTAL, employee, customer, vehicle,
                calculateTotalAmount(vehicle, rentalDays), timestamp);

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE_FOR_RENT) {
            throw new VehicleUnavailableException(
                    "El vehículo con placa " + vehicle.getPlate() +
                            " no está disponible para alquiler. Estado actual: " + vehicle.getStatus());
        }

        if (startDate == null || endDate == null) {
            throw new InvalidTransactionException("Las fechas de inicio y fin del alquiler no pueden ser nulas");
        }

        if (startDate.isAfter(endDate)) {
            throw new InvalidTransactionException(
                    "La fecha de inicio (" + startDate + ") no puede ser posterior a la fecha de fin (" + endDate + ")");
        }

        if (rentalDays <= 0) {
            throw new InvalidTransactionException(
                    "El número de días de alquiler debe ser estrictamente mayor a cero: " + rentalDays);
        }

        this.startDate = startDate;
        this.endDate = endDate;
        this.rentalDays = rentalDays;
    }

    private static double calculateTotalAmount(Vehicle vehicle, int rentalDays) {
        if (vehicle == null || rentalDays <= 0) {
            // El superconstructor validará y arrojará la excepción apropiada
            return 1.0;
        }
        return rentalDays * vehicle.getBasePrice();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getRentalDays() {
        return rentalDays;
    }
}
