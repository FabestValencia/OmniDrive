package com.omnidrive.model;

import com.omnidrive.exception.BlockedUserException;
import com.omnidrive.exception.InvalidTransactionException;
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

@DisplayName("Pruebas Unitarias - Flujos de Alquiler y Devolución en Dealership (T5)")
class DealershipRentalTest {

    private Dealership dealership;
    private Employee activeEmployee;
    private Employee blockedEmployee;
    private Customer customer;
    private Sedan rentalSedan;
    private Instant now;

    @BeforeEach
    void setUp() throws Exception {
        dealership = new Dealership("OmniDrive Renta");

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

        rentalSedan = new Sedan("RNT-100", "Toyota", 2024, 6, 200.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_RENT, 150000.0, "toyota.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                450.0, true, true, true, true);
        dealership.registerVehicle(rentalSedan);

        now = Instant.now();
    }

    // --- PRUEBAS DE ALQUILER (rentVehicle) ---

    @Test
    @DisplayName("rentVehicle debe registrar alquiler exitosamente, calcular tarifa total y conmutar a RENTED (RF-05, RF-12)")
    void testRentVehicleSuccess() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(4);
        int rentalDays = 4;
        double expectedTotal = 4 * 150000.0;

        RentalTransaction txn = dealership.rentVehicle(activeEmployee, customer, "RNT-100",
                start, end, rentalDays, now);

        assertNotNull(txn);
        assertEquals(TransactionType.RENTAL, txn.getType());
        assertEquals("RNT-100", txn.getVehicle().getPlate());
        assertEquals(expectedTotal, txn.getTotalAmount(), 0.001);
        assertEquals(VehicleStatus.RENTED, rentalSedan.getStatus());

        // Debe registrar la transacción en la auditoría
        assertNotNull(dealership.findTransactionById(txn.getId()));
    }

    @Test
    @DisplayName("rentVehicle debe rechazar alquiler realizado por empleado bloqueado (RF-06, RF-15)")
    void testRentVehicleRejectsBlockedEmployee() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);

        assertThrows(BlockedUserException.class, () ->
                dealership.rentVehicle(blockedEmployee, customer, "RNT-100",
                        start, end, 3, now));

        // El estado no debe cambiar
        assertEquals(VehicleStatus.AVAILABLE_FOR_RENT, rentalSedan.getStatus());
    }

    @Test
    @DisplayName("rentVehicle debe rechazar vehículo no disponible para alquiler (RF-08, RF-17)")
    void testRentVehicleRejectsUnavailableVehicle() throws Exception {
        rentalSedan.setStatus(VehicleStatus.AVAILABLE_FOR_SALE);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(2);

        assertThrows(VehicleUnavailableException.class, () ->
                dealership.rentVehicle(activeEmployee, customer, "RNT-100",
                        start, end, 2, now));
    }

    @Test
    @DisplayName("rentVehicle debe rechazar vehículo inexistente")
    void testRentVehicleRejectsNonExistentVehicle() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(2);

        assertThrows(VehicleUnavailableException.class, () ->
                dealership.rentVehicle(activeEmployee, customer, "GHOST-999",
                        start, end, 2, now));
    }

    @Test
    @DisplayName("rentVehicle debe rechazar inconsistencias de fechas o días no positivos (RF-18)")
    void testRentVehicleRejectsInvalidDates() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(2);

        // Fecha de inicio posterior al fin
        assertThrows(InvalidTransactionException.class, () ->
                dealership.rentVehicle(activeEmployee, customer, "RNT-100",
                        end, start, 2, now));

        // Fechas nulas
        assertThrows(InvalidTransactionException.class, () ->
                dealership.rentVehicle(activeEmployee, customer, "RNT-100",
                        null, end, 2, now));

        // Días de alquiler no positivos
        assertThrows(InvalidTransactionException.class, () ->
                dealership.rentVehicle(activeEmployee, customer, "RNT-100",
                        start, end, 0, now));
    }

    // --- PRUEBAS DE DEVOLUCIÓN (returnRentedVehicle) ---

    @Test
    @DisplayName("returnRentedVehicle debe conmutar vehículo de RENTED a AVAILABLE_FOR_RENT exitosamente (RF-13)")
    void testReturnRentedVehicleSuccess() throws Exception {
        // Alquilar primero
        dealership.rentVehicle(activeEmployee, customer, "RNT-100",
                LocalDate.now(), LocalDate.now().plusDays(3), 3, now);
        assertEquals(VehicleStatus.RENTED, rentalSedan.getStatus());

        // Devolver
        Vehicle returned = dealership.returnRentedVehicle("RNT-100");

        assertNotNull(returned);
        assertEquals("RNT-100", returned.getPlate());
        assertEquals(VehicleStatus.AVAILABLE_FOR_RENT, returned.getStatus());
        assertEquals(VehicleStatus.AVAILABLE_FOR_RENT, rentalSedan.getStatus());
    }

    @Test
    @DisplayName("returnRentedVehicle debe rechazar devolución si el vehículo no está en estado RENTED (RF-20)")
    void testReturnRentedVehicleRejectsNotRented() {
        // El vehículo está en AVAILABLE_FOR_RENT (no ha sido alquilado)
        assertThrows(VehicleUnavailableException.class, () ->
                dealership.returnRentedVehicle("RNT-100"));
    }

    @Test
    @DisplayName("returnRentedVehicle debe rechazar placa inexistente o nula")
    void testReturnRentedVehicleRejectsInvalidPlate() {
        assertThrows(InvalidTransactionException.class, () ->
                dealership.returnRentedVehicle(null));
        assertThrows(InvalidTransactionException.class, () ->
                dealership.returnRentedVehicle("   "));
        assertThrows(VehicleUnavailableException.class, () ->
                dealership.returnRentedVehicle("NOT-EXISTS"));
    }
}
