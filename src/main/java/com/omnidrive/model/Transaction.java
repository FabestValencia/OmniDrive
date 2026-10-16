package com.omnidrive.model;

import com.omnidrive.exception.InvalidTransactionException;
import com.omnidrive.model.enums.TransactionType;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Clase base abstracta para todas las transacciones comerciales del concesionario.
 * Garantiza la trazabilidad obligatoria y auditoría inmutable (empleado, cliente, vehículo, fecha y monto).
 */
public abstract class Transaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private final TransactionType type;
    private final Employee employee;
    private final Customer customer;
    private final Vehicle vehicle;
    private final double totalAmount;
    private final Instant timestamp;

    /**
     * Construye una transacción comercial con validación estricta de auditoría.
     *
     * @param id          Identificador único de la transacción (no nulo ni en blanco).
     * @param type        Tipo de transacción (PURCHASE, SALE, RENTAL).
     * @param employee    Empleado responsable de la transacción.
     * @param customer    Cliente involucrado en la transacción.
     * @param vehicle     Vehículo objeto de la transacción.
     * @param totalAmount Monto total pactado (debe ser estrictamente mayor a 0).
     * @param timestamp   Instante temporal en que se registró la transacción.
     * @throws InvalidTransactionException si alguno de los campos es nulo o inválido.
     */
    public Transaction(String id, TransactionType type, Employee employee,
                       Customer customer, Vehicle vehicle, double totalAmount, Instant timestamp)
            throws InvalidTransactionException {
        if (id == null || id.isBlank()) {
            throw new InvalidTransactionException("El identificador de la transacción no puede ser nulo ni estar vacío");
        }
        if (type == null) {
            throw new InvalidTransactionException("El tipo de transacción no puede ser nulo");
        }
        if (employee == null) {
            throw new InvalidTransactionException("El empleado responsable de la transacción no puede ser nulo");
        }
        if (customer == null) {
            throw new InvalidTransactionException("El cliente de la transacción no puede ser nulo");
        }
        if (vehicle == null) {
            throw new InvalidTransactionException("El vehículo de la transacción no puede ser nulo");
        }
        if (totalAmount <= 0) {
            throw new InvalidTransactionException("El monto total de la transacción debe ser mayor a cero: " + totalAmount);
        }
        if (timestamp == null) {
            throw new InvalidTransactionException("La fecha/hora de la transacción no puede ser nula");
        }

        this.id = id.trim();
        this.type = type;
        this.employee = employee;
        this.customer = customer;
        this.vehicle = vehicle;
        this.totalAmount = totalAmount;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", employeeId=" + (employee != null ? employee.getId() : "null") +
                ", customerId=" + (customer != null ? customer.getId() : "null") +
                ", vehiclePlate=" + (vehicle != null ? vehicle.getPlate() : "null") +
                ", totalAmount=" + totalAmount +
                ", timestamp=" + timestamp +
                '}';
    }
}
