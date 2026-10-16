package com.omnidrive.model;

import com.omnidrive.exception.BlockedUserException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.exception.VehicleUnavailableException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransactionType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Aceptación - Escenarios Oficiales de Transacciones (Spec-003)")
class TransactionAcceptanceScenariosTest {

    private Dealership dealership;
    private Employee activeEmployee;
    private Employee blockedEmployee;
    private Customer customer;
    private Instant now;

    @BeforeEach
    void setUp() throws Exception {
        dealership = new Dealership("OmniDrive Acceptance Dealership");

        activeEmployee = new Employee("EMP-101", "Andres", "Cepeda", "3009998877",
                "andres@omnidrive.com", "andres.c", "Pass1234*");
        blockedEmployee = new Employee("EMP-102", "Pedro", "Navaja", "3008887766",
                "pedro@omnidrive.com", "pedro.n", "Pass1234*");
        blockedEmployee.block();

        dealership.registerEmployee(activeEmployee);
        dealership.registerEmployee(blockedEmployee);

        customer = new Customer("CUST-201", "Laura", "Restrepo", "3117776655",
                "laura.restrepo@example.com");
        dealership.registerCustomer(customer);

        now = Instant.now();
    }

    @Test
    @DisplayName("Escenario 1: Venta exitosa de vehículo disponible (RF-01, RF-02, RF-04, RF-11)")
    void testScenario1_SuccessfulSaleOfAvailableVehicle() throws Exception {
        // Dado un vehículo Sedán en estado "Disponible para venta" con precio base de $25,000 y un empleado activo
        Sedan sedan = new Sedan("SED-25K", "Toyota", 2023, 6, 195.0, 1800.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 25000.0, "toyota.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                430.0, true, true, false, false);
        dealership.registerVehicle(sedan);

        // Cuando el empleado registra la venta del vehículo a un cliente registrado por $25,000
        SaleTransaction txn = dealership.sellVehicle(activeEmployee, customer, "SED-25K", 25000.0, now);

        // Entonces el sistema registra la transacción de venta y el vehículo pasa a estado "Vendido"
        assertNotNull(txn);
        assertEquals(TransactionType.SALE, txn.getType());
        assertEquals("SED-25K", txn.getVehicle().getPlate());
        assertEquals(25000.0, txn.getTotalAmount(), 0.001);
        assertEquals(VehicleStatus.SOLD, sedan.getStatus());
        assertSame(txn, dealership.findTransactionById(txn.getId()));
    }

    @Test
    @DisplayName("Escenario 2: Alquiler exitoso y cálculo de tarifa (RF-01, RF-02, RF-05, RF-12)")
    void testScenario2_SuccessfulRentalAndFeeCalculation() throws Exception {
        // Dado una camioneta SUV en estado "Disponible para alquiler" con tarifa diaria de $100 y un empleado activo
        Suv suv = new Suv("SUV-100", "Ford", 2024, 7, 210.0, 2400.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_RENT, 100.0, "ford.jpg",
                true, null, null,
                7, 5, 8, true, true, true,
                550.0, true, true, true, true, true);
        dealership.registerVehicle(suv);

        // Cuando el empleado registra el alquiler a un cliente por 5 días
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(5);
        RentalTransaction txn = dealership.rentVehicle(activeEmployee, customer, "SUV-100",
                start, end, 5, now);

        // Entonces el sistema registra la transacción con un monto calculado de $500 y el vehículo pasa a estado "Alquilado"
        assertNotNull(txn);
        assertEquals(TransactionType.RENTAL, txn.getType());
        assertEquals(500.0, txn.getTotalAmount(), 0.001);
        assertEquals(VehicleStatus.RENTED, suv.getStatus());
        assertEquals(start, txn.getStartDate());
        assertEquals(end, txn.getEndDate());
        assertEquals(5, txn.getRentalDays());
    }

    @Test
    @DisplayName("Escenario 3: Devolución de vehículo alquilado (RF-13)")
    void testScenario3_ReturnOfRentedVehicle() throws Exception {
        // Dado un vehículo en estado "Alquilado"
        Suv suv = new Suv("SUV-200", "Jeep", 2024, 8, 220.0, 2500.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_RENT, 120.0, "jeep.jpg",
                true, null, null,
                5, 5, 6, true, true, true,
                500.0, true, true, true, true, true);
        dealership.registerVehicle(suv);

        dealership.rentVehicle(activeEmployee, customer, "SUV-200",
                LocalDate.now(), LocalDate.now().plusDays(3), 3, now);
        assertEquals(VehicleStatus.RENTED, suv.getStatus());

        // Cuando se registra la devolución del vehículo al concesionario
        Vehicle returned = dealership.returnRentedVehicle("SUV-200");

        // Entonces el vehículo pasa a estado "Disponible para alquiler" quedando habilitado para futuras operaciones
        assertNotNull(returned);
        assertEquals("SUV-200", returned.getPlate());
        assertEquals(VehicleStatus.AVAILABLE_FOR_RENT, returned.getStatus());
        assertEquals(VehicleStatus.AVAILABLE_FOR_RENT, suv.getStatus());
    }

    @Test
    @DisplayName("Escenario 4: Rechazo de compra de vehículo usado sin revisión técnica aprobada (RF-09, RF-14)")
    void testScenario4_RejectPurchaseOfUsedVehicleWithoutTechnicalReview() throws Exception {
        // Dado un vehículo usado cuya revisión técnica está no aprobada o rechazada
        Sedan usedSedanWithoutReview = new Sedan("REJ-001", "Chevrolet", 2017, 5, 170.0, 1500.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.USED,
                VehicleStatus.AVAILABLE_FOR_SALE, 18000.0, "chevy.jpg",
                true, null, null,
                5, 4, 4, true, false, false,
                380.0, false, false, false, false) {
            @Override
            public boolean isTechnicalReviewApproved() {
                return false;
            }
        };

        // Cuando un empleado intenta registrar la compra del vehículo a un cliente
        // Entonces el sistema rechaza la transacción, arroja alerta por revisión técnica rechazada y no almacena el vehículo
        assertThrows(TechnicalReviewRejectedException.class, () ->
                dealership.buyVehicle(activeEmployee, customer, usedSedanWithoutReview,
                        15000.0, VehicleStatus.AVAILABLE_FOR_SALE, now));

        assertNull(dealership.findVehicleByPlate("REJ-001"));
        assertTrue(dealership.getAllTransactions().isEmpty());
    }

    @Test
    @DisplayName("Escenario 5: Rechazo de transacción operada por empleado bloqueado (RF-06, RF-15)")
    void testScenario5_RejectTransactionByBlockedEmployee() throws Exception {
        // Dado un empleado que ha sido bloqueado por el Administrador
        Sedan sedan = new Sedan("BLK-001", "Nissan", 2024, 6, 190.0, 1800.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 22000.0, "nissan.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                420.0, true, true, false, false);
        dealership.registerVehicle(sedan);

        // Cuando se intenta procesar una venta o alquiler asociando a dicho empleado como responsable
        // Entonces el sistema rechaza la transacción e informa que el empleado está bloqueado
        assertThrows(BlockedUserException.class, () ->
                dealership.sellVehicle(blockedEmployee, customer, "BLK-001", 22000.0, now));

        assertEquals(VehicleStatus.AVAILABLE_FOR_SALE, sedan.getStatus());
        assertTrue(dealership.getAllTransactions().isEmpty());
    }

    @Test
    @DisplayName("Escenario 6: Rechazo de venta sobre vehículo no disponible para venta (RF-07, RF-16)")
    void testScenario6_RejectSaleOfUnavailableVehicle() throws Exception {
        // Dado un vehículo que se encuentra en estado "Disponible para alquiler" o "Alquilado"
        Sedan sedanForRent = new Sedan("RNT-ONLY", "Mazda", 2024, 6, 200.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_RENT, 120.0, "mazda.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                420.0, true, true, false, false);
        dealership.registerVehicle(sedanForRent);

        // Cuando se intenta registrar una venta sobre dicho vehículo
        // Entonces el sistema rechaza la operación informando que el vehículo no se encuentra disponible para venta
        assertThrows(VehicleUnavailableException.class, () ->
                dealership.sellVehicle(activeEmployee, customer, "RNT-ONLY", 25000.0, now));

        assertEquals(VehicleStatus.AVAILABLE_FOR_RENT, sedanForRent.getStatus());
        assertTrue(dealership.getAllTransactions().isEmpty());
    }

    @Test
    @DisplayName("Escenario 7: Rechazo de devolución de vehículo no alquilado (RF-20)")
    void testScenario7_RejectReturnOfNonRentedVehicle() throws Exception {
        // Dado un vehículo que se encuentra en estado "Disponible para venta" o "Vendido"
        Sedan sedanForSale = new Sedan("SALE-ONLY", "Hyundai", 2024, 6, 190.0, 1600.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 20000.0, "hyundai.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                400.0, true, true, false, false);
        dealership.registerVehicle(sedanForSale);

        // Cuando se intenta registrar una devolución de alquiler sobre dicho vehículo
        // Entonces el sistema rechaza la operación informando que el vehículo no está en alquiler
        assertThrows(VehicleUnavailableException.class, () ->
                dealership.returnRentedVehicle("SALE-ONLY"));

        assertEquals(VehicleStatus.AVAILABLE_FOR_SALE, sedanForSale.getStatus());
    }
}
