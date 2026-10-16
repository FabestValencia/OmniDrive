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

@DisplayName("Pruebas de Aceptación - Escenarios Oficiales de Reportes (Spec-004)")
class ReportAcceptanceScenariosTest {

    private Dealership dealership;
    private Employee activeEmployee;
    private Employee blockedEmployee;
    private Customer customer;

    @BeforeEach
    void setUp() throws Exception {
        dealership = new Dealership("OmniDrive Central Acceptance");

        activeEmployee = new Employee("EMP-301", "Andres", "Vargas", "3004445566",
                "andres.v@omnidrive.com", "andres.vargas", "Pass1234*");
        blockedEmployee = new Employee("EMP-302", "Esteban", "Morales", "3005556677",
                "esteban.m@omnidrive.com", "esteban.morales", "Pass1234*");

        dealership.registerEmployee(activeEmployee);
        dealership.registerEmployee(blockedEmployee);

        customer = new Customer("CUST-501", "Camila", "Duarte", "3118889900",
                "camila.duarte@example.com");
        dealership.registerCustomer(customer);
    }

    @Test
    @DisplayName("Escenario 1: Generación exitosa de reporte con actividad mixta y cálculo de comisiones (RF-01 al RF-06)")
    void testScenario1_SuccessfulReportWithMixedActivityAndCommissions() throws Exception {
        // Dado un empleado activo que ha realizado en el mes:
        // - 1 compra por $30,000 (nuevo en venta)
        Sedan purchaseSedan = new Sedan("PUR-001", "Toyota", 2024, 6, 200.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 30000.0, "toyota.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                450.0, true, true, true, true);
        Instant t1 = Instant.parse("2026-03-05T10:00:00Z");
        dealership.buyVehicle(activeEmployee, customer, purchaseSedan, 30000.0,
                VehicleStatus.AVAILABLE_FOR_SALE, t1);

        // - 2 ventas por un total de $70,000 ($40,000 y $30,000)
        Sedan saleSedan1 = new Sedan("SAL-001", "Mazda", 2024, 6, 210.0, 2200.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 40000.0, "mazda.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                420.0, true, true, true, true);
        dealership.registerVehicle(saleSedan1);
        Instant t2 = Instant.parse("2026-03-10T11:00:00Z");
        dealership.sellVehicle(activeEmployee, customer, "SAL-001", 40000.0, t2);

        Sedan saleSedan2 = new Sedan("SAL-002", "Nissan", 2024, 6, 190.0, 1800.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 30000.0, "nissan.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                400.0, true, true, false, false);
        dealership.registerVehicle(saleSedan2);
        Instant t3 = Instant.parse("2026-03-15T15:00:00Z");
        dealership.sellVehicle(activeEmployee, customer, "SAL-002", 30000.0, t3);

        // - 1 alquiler por $1,000 (10 días x $100/día)
        Sedan rentalSedan = new Sedan("RNT-001", "Kia", 2024, 6, 180.0, 1600.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_RENT, 100.0, "kia.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                380.0, true, true, false, false);
        dealership.registerVehicle(rentalSedan);
        Instant t4 = Instant.parse("2026-03-20T09:00:00Z");
        dealership.rentVehicle(activeEmployee, customer, "RNT-001",
                LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 30), 10, t4);

        // Cuando el Administrador genera el reporte del empleado para dicho mes
        Instant start = Instant.parse("2026-03-01T00:00:00Z");
        Instant end = Instant.parse("2026-03-31T23:59:59Z");
        EmployeeReport report = dealership.generateEmployeeReport(activeEmployee.getId(), start, end);

        // Entonces el reporte consolida:
        assertNotNull(report);
        assertEquals(4, report.getTotalTransactionsCount());
        assertEquals(1, report.getPurchaseCount());
        assertEquals(2, report.getSaleCount());
        assertEquals(1, report.getRentalCount());

        assertEquals(30000.0, report.getTotalPurchaseAmount(), 0.001);
        assertEquals(70000.0, report.getTotalSaleAmount(), 0.001);
        assertEquals(1000.0, report.getTotalRentalAmount(), 0.001);
        assertEquals(101000.0, report.getGrandTotalAmount(), 0.001);

        // Comisión por ventas (2% de $70,000) = $1,400
        assertEquals(1400.0, report.getSalesCommission(), 0.001);

        // Lista cronológica con las 4 transacciones detalladas
        assertEquals(4, report.getTransactions().size());
    }

    @Test
    @DisplayName("Escenario 2: Generación de reporte para período sin actividad comercial (RF-07)")
    void testScenario2_ReportForPeriodWithNoActivity() throws Exception {
        // Dado un empleado activo que no ha efectuado transacciones entre el 1 y el 15 de marzo
        LocalDate start = LocalDate.of(2026, 3, 1);
        LocalDate end = LocalDate.of(2026, 3, 15);

        // Cuando el Administrador genera el reporte para ese rango temporal
        EmployeeReport report = dealership.generateEmployeeReport(activeEmployee.getId(), start, end);

        // Entonces el sistema genera un reporte con contadores y montos en 0 y lista vacía
        assertNotNull(report);
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
    @DisplayName("Escenario 3: Auditoría histórica sobre empleado bloqueado (RF-08)")
    void testScenario3_HistoricalAuditOnBlockedEmployee() throws Exception {
        // Dado un empleado que realizó ventas el mes pasado y fue bloqueado posteriormente por el Administrador
        Sedan historicalSedan = new Sedan("HIST-001", "Chevrolet", 2024, 6, 200.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 50000.0, "chevy.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                430.0, true, true, false, false);
        dealership.registerVehicle(historicalSedan);

        Instant txTime = Instant.parse("2026-02-15T10:00:00Z");
        dealership.sellVehicle(blockedEmployee, customer, "HIST-001", 50000.0, txTime);

        // Bloqueo posterior
        dealership.blockEmployee(blockedEmployee.getId());
        assertTrue(blockedEmployee.isBlocked());

        // Cuando el Administrador solicita el reporte de dicho empleado para el mes pasado
        Instant start = Instant.parse("2026-02-01T00:00:00Z");
        Instant end = Instant.parse("2026-02-28T23:59:59Z");
        EmployeeReport report = dealership.generateEmployeeReport(blockedEmployee.getId(), start, end);

        // Entonces el sistema genera exitosamente el reporte con las transacciones históricas, refleja ventas y comisiones e indica estado BLOCKED
        assertNotNull(report);
        assertTrue(report.getEmployee().isBlocked());
        assertEquals(1, report.getTotalTransactionsCount());
        assertEquals(1, report.getSaleCount());
        assertEquals(50000.0, report.getTotalSaleAmount(), 0.001);
        assertEquals(1000.0, report.getSalesCommission(), 0.001); // 2% de 50,000 = 1,000
    }

    @Test
    @DisplayName("Escenario 4: Rechazo ante rango temporal invertido (RF-12)")
    void testScenario4_RejectInvertedDateRange() {
        // Dado un empleado registrado en el concesionario
        // Cuando el Administrador solicita un reporte con fecha de inicio posterior a la fecha de fin
        LocalDate start = LocalDate.of(2026, 6, 30);
        LocalDate end = LocalDate.of(2026, 6, 1);

        // Entonces el sistema rechaza la solicitud e informa que el rango de fechas es inconsistente
        assertThrows(InvalidReportDateRangeException.class, () ->
                dealership.generateEmployeeReport(activeEmployee.getId(), start, end));
    }

    @Test
    @DisplayName("Escenario 5: Rechazo ante empleado inexistente o nulo (RF-11)")
    void testScenario5_RejectNonExistentOrNullEmployee() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        // Dado un identificador que no existe o es nulo
        // Cuando se intenta generar un reporte con dicho identificador
        // Entonces el sistema rechaza la operación informando que el empleado no existe
        assertThrows(InvalidUserDataException.class, () ->
                dealership.generateEmployeeReport("NON-EXISTENT-ID", start, end));

        assertThrows(InvalidUserDataException.class, () ->
                dealership.generateEmployeeReport(null, start, end));
    }
}
