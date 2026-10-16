package com.omnidrive.model;

import com.omnidrive.exception.BlockedUserException;
import com.omnidrive.exception.DuplicateVehicleException;
import com.omnidrive.exception.InvalidTransactionException;
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

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Flujos de Compra y Venta en Dealership (T4)")
class DealershipBuySellTest {

    private Dealership dealership;
    private Employee activeEmployee;
    private Employee blockedEmployee;
    private Customer customer;
    private Sedan newSedan;
    private Sedan usedSedanApproved;
    private Instant now;

    @BeforeEach
    void setUp() throws Exception {
        dealership = new Dealership("OmniDrive Central");

        activeEmployee = new Employee("EMP-001", "Carlos", "Perez", "3001234567",
                "carlos@omnidrive.com", "carlos.perez", "Pass1234!");
        blockedEmployee = new Employee("EMP-002", "Juan", "Gomez", "3007654321",
                "juan@omnidrive.com", "juan.gomez", "Pass1234!");
        blockedEmployee.block();

        dealership.registerEmployee(activeEmployee);
        dealership.registerEmployee(blockedEmployee);

        customer = new Customer("CUST-001", "Maria", "Lopez", "3115554433",
                "maria.lopez@example.com");
        dealership.registerCustomer(customer);

        newSedan = new Sedan("ABC-111", "Toyota", 2024, 6, 200.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 60000.0, "toyota.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                450.0, true, true, true, true);

        usedSedanApproved = new Sedan("XYZ-999", "Mazda", 2021, 6, 190.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.USED,
                VehicleStatus.AVAILABLE_FOR_SALE, 35000.0, "mazda.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                420.0, true, true, false, false);

        now = Instant.now();
    }

    // --- PRUEBAS DE COMPRA (buyVehicle) ---

    @Test
    @DisplayName("buyVehicle debe comprar vehículo nuevo exitosamente e ingresarlo al inventario con el estado indicado (RF-10)")
    void testBuyVehicleSuccess() throws Exception {
        PurchaseTransaction txn = dealership.buyVehicle(activeEmployee, customer, newSedan,
                55000.0, VehicleStatus.AVAILABLE_FOR_SALE, now);

        assertNotNull(txn);
        assertEquals("TXN-00001", txn.getId());
        assertEquals(TransactionType.PURCHASE, txn.getType());
        assertEquals(55000.0, txn.getTotalAmount(), 0.001);
        assertEquals(VehicleStatus.AVAILABLE_FOR_SALE, newSedan.getStatus());

        // Verificar que ingresó al inventario
        assertNotNull(dealership.findVehicleByPlate("ABC-111"));
        assertEquals(1, dealership.getAllTransactions().size());
    }

    @Test
    @DisplayName("buyVehicle debe permitir asignar estado inicial AVAILABLE_FOR_RENT a vehículo comprado")
    void testBuyVehicleAvailableForRent() throws Exception {
        PurchaseTransaction txn = dealership.buyVehicle(activeEmployee, customer, usedSedanApproved,
                30000.0, VehicleStatus.AVAILABLE_FOR_RENT, now);

        assertEquals(VehicleStatus.AVAILABLE_FOR_RENT, usedSedanApproved.getStatus());
        assertEquals(VehicleStatus.AVAILABLE_FOR_RENT, txn.getInitialInventoryStatus());
    }

    @Test
    @DisplayName("buyVehicle debe rechazar compras realizadas por empleados bloqueados (RF-06, RF-15)")
    void testBuyVehicleRejectsBlockedEmployee() {
        assertThrows(BlockedUserException.class, () ->
                dealership.buyVehicle(blockedEmployee, customer, newSedan,
                        55000.0, VehicleStatus.AVAILABLE_FOR_SALE, now));
    }

    @Test
    @DisplayName("buyVehicle debe rechazar vehículos con placa duplicada en el inventario")
    void testBuyVehicleRejectsDuplicatePlate() throws Exception {
        dealership.buyVehicle(activeEmployee, customer, newSedan,
                55000.0, VehicleStatus.AVAILABLE_FOR_SALE, now);

        Sedan duplicatePlateSedan = new Sedan("ABC-111", "Nissan", 2024, 6, 180.0, 1600.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 45000.0, "nissan.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                400.0, true, true, false, false);

        assertThrows(DuplicateVehicleException.class, () ->
                dealership.buyVehicle(activeEmployee, customer, duplicatePlateSedan,
                        40000.0, VehicleStatus.AVAILABLE_FOR_SALE, now));
    }

    @Test
    @DisplayName("buyVehicle debe rechazar compra de vehículo usado sin revisión técnica aprobada (RF-09, RF-14)")
    void testBuyVehicleRejectsUsedWithoutReview() throws Exception {
        Sedan usedWithoutReview = new Sedan("BAD-000", "Chevrolet", 2018, 5, 160.0, 1400.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.USED,
                VehicleStatus.AVAILABLE_FOR_SALE, 20000.0, "chevy.jpg",
                true, null, null,
                5, 4, 4, true, false, false,
                380.0, false, false, false, false) {
            @Override
            public boolean isTechnicalReviewApproved() {
                return false;
            }
        };

        assertThrows(TechnicalReviewRejectedException.class, () ->
                dealership.buyVehicle(activeEmployee, customer, usedWithoutReview,
                        18000.0, VehicleStatus.AVAILABLE_FOR_SALE, now));
    }

    // --- PRUEBAS DE VENTA (sellVehicle) ---

    @Test
    @DisplayName("sellVehicle debe vender exitosamente un vehículo disponible para venta y conmutar a SOLD (RF-11)")
    void testSellVehicleSuccess() throws Exception {
        dealership.registerVehicle(newSedan);

        SaleTransaction txn = dealership.sellVehicle(activeEmployee, customer, "ABC-111", 60000.0, now);

        assertNotNull(txn);
        assertEquals(TransactionType.SALE, txn.getType());
        assertEquals("ABC-111", txn.getVehicle().getPlate());
        assertEquals(VehicleStatus.SOLD, newSedan.getStatus());
        assertEquals(60000.0, txn.getTotalAmount(), 0.001);
    }

    @Test
    @DisplayName("sellVehicle debe rechazar venta realizada por empleado bloqueado (RF-06, RF-15)")
    void testSellVehicleRejectsBlockedEmployee() throws Exception {
        dealership.registerVehicle(newSedan);

        assertThrows(BlockedUserException.class, () ->
                dealership.sellVehicle(blockedEmployee, customer, "ABC-111", 60000.0, now));
        // Confirmar que el estado del vehículo no cambió
        assertEquals(VehicleStatus.AVAILABLE_FOR_SALE, newSedan.getStatus());
    }

    @Test
    @DisplayName("sellVehicle debe rechazar venta si el vehículo no está disponible para venta (RF-07, RF-16)")
    void testSellVehicleRejectsUnavailableVehicle() throws Exception {
        newSedan.setStatus(VehicleStatus.AVAILABLE_FOR_RENT);
        dealership.registerVehicle(newSedan);

        assertThrows(VehicleUnavailableException.class, () ->
                dealership.sellVehicle(activeEmployee, customer, "ABC-111", 60000.0, now));
    }

    @Test
    @DisplayName("sellVehicle debe rechazar venta si la placa no existe en el inventario")
    void testSellVehicleRejectsNonExistentVehicle() {
        assertThrows(VehicleUnavailableException.class, () ->
                dealership.sellVehicle(activeEmployee, customer, "NON-EXISTENT", 50000.0, now));
    }

    @Test
    @DisplayName("sellVehicle debe rechazar monto no positivo (RF-04)")
    void testSellVehicleRejectsInvalidAmount() throws Exception {
        dealership.registerVehicle(newSedan);

        assertThrows(InvalidTransactionException.class, () ->
                dealership.sellVehicle(activeEmployee, customer, "ABC-111", 0.0, now));
        assertThrows(InvalidTransactionException.class, () ->
                dealership.sellVehicle(activeEmployee, customer, "ABC-111", -500.0, now));
    }
}
