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

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Motor de Reportes en Dealership (T2)")
class DealershipReportTest {

    private Dealership dealership;
    private Employee activeEmp;
    private Employee blockedEmp;
    private Customer customer;
    private Sedan sedan;

    @BeforeEach
    void setUp() throws Exception {
        dealership = new Dealership("OmniDrive Reports Branch");

        activeEmp = new Employee("EMP-001", "Carlos", "Perez", "3001234567",
                "carlos@omnidrive.com", "carlos.p", "Pass1234!");
        blockedEmp = new Employee("EMP-002", "Juan", "Bloqueado", "3007654321",
                "juan@omnidrive.com", "juan.b", "Pass1234!");

        dealership.registerEmployee(activeEmp);
        dealership.registerEmployee(blockedEmp);

        customer = new Customer("CUST-001", "Laura", "Restrepo", "3119876543",
                "laura@gmail.com");
        dealership.registerCustomer(customer);

        sedan = new Sedan("SED-999", "Toyota", 2024, 6, 200.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 40000.0, "toyota.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                450.0, true, true, true, true);
        dealership.registerVehicle(sedan);
    }

    @Test
    @DisplayName("generateEmployeeReport debe consolidar transacciones dentro del intervalo cerrado [start, end]")
    void testGenerateEmployeeReportWithTransactions() throws Exception {
        Instant t1 = Instant.parse("2026-03-05T10:00:00Z"); // dentro
        Instant t2 = Instant.parse("2026-03-20T14:00:00Z"); // dentro
        Instant tOutside = Instant.parse("2026-04-05T10:00:00Z"); // fuera del rango

        dealership.sellVehicle(activeEmp, customer, "SED-999", 40000.0, t1);

        Sedan sedan2 = new Sedan("SED-888", "Mazda", 2024, 6, 210.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_RENT, 100.0, "mazda.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                400.0, true, true, true, true);
        dealership.registerVehicle(sedan2);
        dealership.rentVehicle(activeEmp, customer, "SED-888",
                LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 25), 5, t2);

        // Transacción fuera de rango
        Sedan sedan3 = new Sedan("SED-777", "Nissan", 2024, 6, 190.0, 1800.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 30000.0, "nissan.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                400.0, true, true, false, false);
        dealership.registerVehicle(sedan3);
        dealership.sellVehicle(activeEmp, customer, "SED-777", 30000.0, tOutside);

        Instant rangeStart = Instant.parse("2026-03-01T00:00:00Z");
        Instant rangeEnd = Instant.parse("2026-03-31T23:59:59Z");

        EmployeeReport report = dealership.generateEmployeeReport(activeEmp.getId(), rangeStart, rangeEnd);

        assertNotNull(report);
        assertEquals(2, report.getTotalTransactionsCount());
        assertEquals(1, report.getSaleCount());
        assertEquals(1, report.getRentalCount());
        assertEquals(0, report.getPurchaseCount());
        assertEquals(40000.0, report.getTotalSaleAmount(), 0.001);
        assertEquals(500.0, report.getTotalRentalAmount(), 0.001); // 5 días x $100
        assertEquals(40500.0, report.getGrandTotalAmount(), 0.001);
        assertEquals(800.0, report.getSalesCommission(), 0.001); // 2% de 40,000 = 800
    }

    @Test
    @DisplayName("generateEmployeeReport debe permitir consultar historial de empleado actualmente bloqueado (RF-08)")
    void testGenerateReportOnBlockedEmployee() throws Exception {
        Instant txTime = Instant.parse("2026-02-10T11:00:00Z");
        dealership.sellVehicle(blockedEmp, customer, "SED-999", 40000.0, txTime);

        // Bloquear al empleado posteriormente
        dealership.blockEmployee(blockedEmp.getId());
        assertTrue(blockedEmp.isBlocked());

        Instant start = Instant.parse("2026-02-01T00:00:00Z");
        Instant end = Instant.parse("2026-02-28T23:59:59Z");

        EmployeeReport report = dealership.generateEmployeeReport(blockedEmp.getId(), start, end);

        assertNotNull(report);
        assertTrue(report.getEmployee().isBlocked());
        assertEquals(1, report.getSaleCount());
        assertEquals(40000.0, report.getTotalSaleAmount(), 0.001);
    }

    @Test
    @DisplayName("generateEmployeeReport debe funcionar con sobrecarga LocalDate")
    void testGenerateReportWithLocalDateOverload() throws Exception {
        Instant txTime = Instant.parse("2026-05-15T12:00:00Z");
        dealership.sellVehicle(activeEmp, customer, "SED-999", 40000.0, txTime);

        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 31);

        EmployeeReport report = dealership.generateEmployeeReport(activeEmp.getId(), startDate, endDate);

        assertNotNull(report);
        assertEquals(1, report.getTotalTransactionsCount());
        assertEquals(40000.0, report.getTotalSaleAmount(), 0.001);
    }

    @Test
    @DisplayName("generateEmployeeReport debe rechazar empleado inexistente o nulo (RF-11)")
    void testRejectNonExistentEmployee() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-01-31T23:59:59Z");

        assertThrows(InvalidUserDataException.class, () ->
                dealership.generateEmployeeReport("NON-EXISTENT", start, end));
        assertThrows(InvalidUserDataException.class, () ->
                dealership.generateEmployeeReport(null, start, end));
        assertThrows(InvalidUserDataException.class, () ->
                dealership.generateEmployeeReport("   ", start, end));
    }

    @Test
    @DisplayName("generateEmployeeReport debe rechazar fechas nulas o rango invertido (RF-12, RF-13)")
    void testRejectInvalidDateRange() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-01-31T23:59:59Z");

        assertThrows(InvalidReportDateRangeException.class, () ->
                dealership.generateEmployeeReport(activeEmp.getId(), null, end));
        assertThrows(InvalidReportDateRangeException.class, () ->
                dealership.generateEmployeeReport(activeEmp.getId(), start, null));
        assertThrows(InvalidReportDateRangeException.class, () ->
                dealership.generateEmployeeReport(activeEmp.getId(), end, start));
    }
}
