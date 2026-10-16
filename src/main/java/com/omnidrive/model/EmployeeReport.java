package com.omnidrive.model;

import com.omnidrive.exception.InvalidReportDateRangeException;
import com.omnidrive.exception.InvalidUserDataException;
import com.omnidrive.model.enums.TransactionType;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa un reporte gerencial inmutable con las métricas comerciales y de auditoría
 * consolidadas de un empleado para un intervalo temporal determinado.
 */
public class EmployeeReport implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Porcentaje estándar del concesionario para comisiones por venta (2%).
     */
    public static final double SALES_COMMISSION_RATE = 0.02;

    private final Employee employee;
    private final Instant startInstant;
    private final Instant endInstant;
    private final Instant generatedAt;
    private final List<Transaction> transactions;

    private final int purchaseCount;
    private final int saleCount;
    private final int rentalCount;
    private final int totalTransactionsCount;

    private final double totalPurchaseAmount;
    private final double totalSaleAmount;
    private final double totalRentalAmount;
    private final double grandTotalAmount;
    private final double salesCommission;

    /**
     * Construye un reporte gerencial consolidado para un empleado y rango temporal.
     *
     * @param employee     Empleado evaluado.
     * @param startInstant Límite inferior del período.
     * @param endInstant   Límite superior del período.
     * @param generatedAt  Fecha y hora de generación del reporte.
     * @param txns         Lista de transacciones registradas en el período.
     * @throws InvalidUserDataException            si el empleado es nulo.
     * @throws InvalidReportDateRangeException si las fechas son nulas o la fecha de inicio es posterior a la de fin.
     */
    public EmployeeReport(Employee employee, Instant startInstant, Instant endInstant,
                          Instant generatedAt, List<Transaction> txns)
            throws InvalidUserDataException, InvalidReportDateRangeException {
        if (employee == null) {
            throw new InvalidUserDataException("El empleado del reporte no puede ser nulo.");
        }
        if (startInstant == null || endInstant == null) {
            throw new InvalidReportDateRangeException("Las fechas de inicio y fin del reporte no pueden ser nulas.");
        }
        if (startInstant.isAfter(endInstant)) {
            throw new InvalidReportDateRangeException("La fecha de inicio (" + startInstant +
                    ") no puede ser posterior a la fecha de fin (" + endInstant + ").");
        }

        this.employee = employee;
        this.startInstant = startInstant;
        this.endInstant = endInstant;
        this.generatedAt = generatedAt != null ? generatedAt : Instant.now();

        List<Transaction> sourceList = txns != null ? new ArrayList<>(txns) : new ArrayList<>();
        this.transactions = Collections.unmodifiableList(sourceList);

        int purchases = 0;
        int sales = 0;
        int rentals = 0;
        double purchaseSum = 0.0;
        double saleSum = 0.0;
        double rentalSum = 0.0;

        for (Transaction txn : sourceList) {
            if (txn.getType() == TransactionType.PURCHASE) {
                purchases++;
                purchaseSum += txn.getTotalAmount();
            } else if (txn.getType() == TransactionType.SALE) {
                sales++;
                saleSum += txn.getTotalAmount();
            } else if (txn.getType() == TransactionType.RENTAL) {
                rentals++;
                rentalSum += txn.getTotalAmount();
            }
        }

        this.purchaseCount = purchases;
        this.saleCount = sales;
        this.rentalCount = rentals;
        this.totalTransactionsCount = sourceList.size();

        this.totalPurchaseAmount = purchaseSum;
        this.totalSaleAmount = saleSum;
        this.totalRentalAmount = rentalSum;
        this.grandTotalAmount = purchaseSum + saleSum + rentalSum;

        this.salesCommission = saleSum * SALES_COMMISSION_RATE;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Instant getStartInstant() {
        return startInstant;
    }

    public Instant getEndInstant() {
        return endInstant;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public int getPurchaseCount() {
        return purchaseCount;
    }

    public int getSaleCount() {
        return saleCount;
    }

    public int getRentalCount() {
        return rentalCount;
    }

    public int getTotalTransactionsCount() {
        return totalTransactionsCount;
    }

    public double getTotalPurchaseAmount() {
        return totalPurchaseAmount;
    }

    public double getTotalSaleAmount() {
        return totalSaleAmount;
    }

    public double getTotalRentalAmount() {
        return totalRentalAmount;
    }

    public double getGrandTotalAmount() {
        return grandTotalAmount;
    }

    public double getSalesCommission() {
        return salesCommission;
    }

    @Override
    public String toString() {
        return "EmployeeReport{" +
                "employee=" + employee.getUsername() +
                ", startInstant=" + startInstant +
                ", endInstant=" + endInstant +
                ", totalTransactions=" + totalTransactionsCount +
                ", grandTotal=" + grandTotalAmount +
                ", salesCommission=" + salesCommission +
                '}';
    }
}
