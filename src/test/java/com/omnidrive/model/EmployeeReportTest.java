package com.omnidrive.model;

import com.omnidrive.exception.InvalidReportDateRangeException;
import com.omnidrive.exception.InvalidUserDataException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Modelo Inmutable EmployeeReport (T1)")
class EmployeeReportTest {

    private Employee employee;
    private Customer customer;
    private Sedan sedan;
    private Instant start;
    private Instant end;
    private Instant generatedAt;

    @BeforeEach
    void setUp() throws Exception {
        employee = new Employee("EMP-001", "Carlos", "Perez", "3001234567",
                "carlos@omnidrive.com", "carlos.p", "Pass1234!");
        customer = new Customer("CUST-001", "Maria", "Lopez", "3115554433",
                "maria@example.com");
        sedan = new Sedan("SED-123", "Toyota", 2024, 6, 200.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 30000.0, "toyota.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                450.0, true, true, true, true);

        start = Instant.parse("2026-01-01T00:00:00Z");
        end = Instant.parse("2026-01-31T23:59:59Z");
        generatedAt = Instant.parse("2026-02-01T10:00:00Z");
    }

    @Test
    @DisplayName("Debe instanciar correctamente y calcular totales, subtotales y comisión del 2% (RF-01 al RF-06)")
    void testEmployeeReportCalculations() throws Exception {
        Instant t1Time = Instant.parse("2026-01-05T10:00:00Z");
        Instant t2Time = Instant.parse("2026-01-10T12:00:00Z");
        Instant t3Time = Instant.parse("2026-01-15T15:00:00Z");
        Instant t4Time = Instant.parse("2026-01-20T09:00:00Z");

        // 1 compra por $30,000
        PurchaseTransaction purchase = new PurchaseTransaction("TXN-P1", employee, customer,
                sedan, 30000.0, t1Time, VehicleStatus.AVAILABLE_FOR_SALE);

        // 2 ventas: $40,000 y $30,000 (total ventas = $70,000)
        SaleTransaction sale1 = new SaleTransaction("TXN-S1", employee, customer, sedan, 40000.0, t2Time);
        SaleTransaction sale2 = new SaleTransaction("TXN-S2", employee, customer, sedan, 30000.0, t3Time);

        // 1 alquiler por $1,000 (10 días x $100/día)
        Sedan rentalSedan = new Sedan("RNT-001", "Toyota", 2024, 6, 200.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_RENT, 100.0, "toyota.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                450.0, true, true, true, true);
        RentalTransaction rental = new RentalTransaction("TXN-R1", employee, customer, rentalSedan,
                t4Time, LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 30), 10);

        List<Transaction> txns = List.of(purchase, sale1, sale2, rental);

        EmployeeReport report = new EmployeeReport(employee, start, end, generatedAt, txns);

        assertSame(employee, report.getEmployee());
        assertEquals(start, report.getStartInstant());
        assertEquals(end, report.getEndInstant());
        assertEquals(generatedAt, report.getGeneratedAt());

        // Conteos (RF-02)
        assertEquals(4, report.getTotalTransactionsCount());
        assertEquals(1, report.getPurchaseCount());
        assertEquals(2, report.getSaleCount());
        assertEquals(1, report.getRentalCount());

        // Montos (RF-03, RF-04)
        assertEquals(30000.0, report.getTotalPurchaseAmount(), 0.001);
        assertEquals(70000.0, report.getTotalSaleAmount(), 0.001);
        assertEquals(1000.0, report.getTotalRentalAmount(), 0.001);
        assertEquals(101000.0, report.getGrandTotalAmount(), 0.001);

        // Comisión por ventas: 2% de 70,000 = 1,400 (RF-05)
        assertEquals(1400.0, report.getSalesCommission(), 0.001);

        // Detalle de transacciones (RF-06)
        assertEquals(4, report.getTransactions().size());
    }

    @Test
    @DisplayName("Debe generar reporte con contadores y montos en 0 para lista vacía (RF-07)")
    void testEmptyReport() throws Exception {
        EmployeeReport report = new EmployeeReport(employee, start, end, generatedAt, new ArrayList<>());

        assertEquals(0, report.getTotalTransactionsCount());
        assertEquals(0, report.getPurchaseCount());
        assertEquals(0, report.getSaleCount());
        assertEquals(0, report.getRentalCount());

        assertEquals(0.0, report.getTotalPurchaseAmount(), 0.001);
        assertEquals(0.0, report.getTotalSaleAmount(), 0.001);
        assertEquals(0.0, report.getTotalRentalAmount(), 0.001);
        assertEquals(0.0, report.getGrandTotalAmount(), 0.001);
        assertEquals(0.0, report.getSalesCommission(), 0.001);

        assertTrue(report.getTransactions().isEmpty());
    }

    @Test
    @DisplayName("La lista de transacciones debe ser inmodificable")
    void testTransactionsListIsUnmodifiable() throws Exception {
        EmployeeReport report = new EmployeeReport(employee, start, end, generatedAt, null);

        assertThrows(UnsupportedOperationException.class, () ->
                report.getTransactions().add(null));
    }

    @Test
    @DisplayName("Debe rechazar fechas nulas o fecha de inicio posterior a la fecha de fin (RF-12, RF-13)")
    void testRejectInvalidDateRange() {
        assertThrows(InvalidReportDateRangeException.class, () ->
                new EmployeeReport(employee, null, end, generatedAt, List.of()));

        assertThrows(InvalidReportDateRangeException.class, () ->
                new EmployeeReport(employee, start, null, generatedAt, List.of()));

        assertThrows(InvalidReportDateRangeException.class, () ->
                new EmployeeReport(employee, end, start, generatedAt, List.of())); // start posterior a end
    }

    @Test
    @DisplayName("Debe rechazar empleado nulo")
    void testRejectNullEmployee() {
        assertThrows(InvalidUserDataException.class, () ->
                new EmployeeReport(null, start, end, generatedAt, List.of()));
    }

    @Test
    @DisplayName("EmployeeReport e InvalidReportDateRangeException deben ser serializables")
    void testSerialization() throws Exception {
        EmployeeReport report = new EmployeeReport(employee, start, end, generatedAt, List.of());
        InvalidReportDateRangeException ex = new InvalidReportDateRangeException("Rango inválido");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(report);
            oos.writeObject(ex);
        }

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            Object repObj = ois.readObject();
            Object exObj = ois.readObject();

            assertTrue(repObj instanceof EmployeeReport);
            assertTrue(exObj instanceof InvalidReportDateRangeException);
            assertEquals(report.getSalesCommission(), ((EmployeeReport) repObj).getSalesCommission(), 0.001);
        }
    }
}
