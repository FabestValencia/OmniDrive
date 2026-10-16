package com.omnidrive.model;

import com.omnidrive.exception.DuplicateTransactionException;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Consultas, Autogeneración de IDs y Trazabilidad (T6)")
class DealershipTransactionQueryTest {

    private Dealership dealership;
    private Employee emp1;
    private Employee emp2;
    private Customer customer;
    private Sedan sedan1;
    private Sedan sedan2;
    private Instant now;

    @BeforeEach
    void setUp() throws Exception {
        dealership = new Dealership("OmniDrive Queries");

        emp1 = new Employee("EMP-001", "Carlos", "Perez", "3001111111",
                "carlos@omnidrive.com", "carlos.p", "Pass1234!");
        emp2 = new Employee("EMP-002", "Ana", "Ruiz", "3002222222",
                "ana@omnidrive.com", "ana.r", "Pass1234!");
        dealership.registerEmployee(emp1);
        dealership.registerEmployee(emp2);

        customer = new Customer("CUST-001", "Lucia", "Torres", "3113333333",
                "lucia@example.com");
        dealership.registerCustomer(customer);

        sedan1 = new Sedan("SED-001", "Toyota", 2024, 6, 200.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 50000.0, "toyota.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                450.0, true, true, true, true);

        sedan2 = new Sedan("SED-002", "Mazda", 2024, 6, 210.0, 2500.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_RENT, 120000.0, "mazda.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                420.0, true, true, true, true);

        dealership.registerVehicle(sedan1);
        dealership.registerVehicle(sedan2);

        now = Instant.now();
    }

    @Test
    @DisplayName("Autogeneración de IDs debe seguir el formato secuencial TXN-%05d (RF-01, RF-19)")
    void testTransactionIdAutogenerationSequence() throws Exception {
        PurchaseTransaction t1 = dealership.buyVehicle(emp1, customer,
                new Sedan("NEW-101", "Kia", 2024, 6, 180.0, 1600.0,
                        FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.NEW,
                        VehicleStatus.AVAILABLE_FOR_SALE, 30000.0, "kia.jpg",
                        true, null, null,
                        5, 4, 6, true, true, true,
                        400.0, true, true, false, false),
                25000.0, VehicleStatus.AVAILABLE_FOR_SALE, now);

        SaleTransaction t2 = dealership.sellVehicle(emp2, customer, "SED-001", 52000.0, now);

        RentalTransaction t3 = dealership.rentVehicle(emp1, customer, "SED-002",
                LocalDate.now(), LocalDate.now().plusDays(2), 2, now);

        assertEquals("TXN-00001", t1.getId());
        assertEquals("TXN-00002", t2.getId());
        assertEquals("TXN-00003", t3.getId());
    }

    @Test
    @DisplayName("findTransactionById debe encontrar la transacción existente y retornar null para IDs inexistentes o nulos")
    void testFindTransactionById() throws Exception {
        SaleTransaction sale = dealership.sellVehicle(emp1, customer, "SED-001", 50000.0, now);

        Transaction found = dealership.findTransactionById(sale.getId());
        assertNotNull(found);
        assertEquals(sale.getId(), found.getId());
        assertSame(sale, found);

        assertNull(dealership.findTransactionById("TXN-99999"));
        assertNull(dealership.findTransactionById(null));
        assertNull(dealership.findTransactionById("   "));
    }

    @Test
    @DisplayName("getAllTransactions debe retornar una lista inmodificable con todas las transacciones registradas")
    void testGetAllTransactionsUnmodifiable() throws Exception {
        dealership.sellVehicle(emp1, customer, "SED-001", 50000.0, now);
        dealership.rentVehicle(emp2, customer, "SED-002",
                LocalDate.now(), LocalDate.now().plusDays(1), 1, now);

        List<Transaction> list = dealership.getAllTransactions();
        assertEquals(2, list.size());

        // Debe ser inmodificable
        assertThrows(UnsupportedOperationException.class, () ->
                list.remove(0));
    }

    @Test
    @DisplayName("getTransactionsByEmployee debe filtrar con precisión las transacciones por ID de empleado")
    void testGetTransactionsByEmployee() throws Exception {
        dealership.sellVehicle(emp1, customer, "SED-001", 50000.0, now);
        dealership.rentVehicle(emp2, customer, "SED-002",
                LocalDate.now(), LocalDate.now().plusDays(3), 3, now);

        List<Transaction> emp1Txns = dealership.getTransactionsByEmployee(emp1.getId());
        assertEquals(1, emp1Txns.size());
        assertEquals(emp1.getId(), emp1Txns.get(0).getEmployee().getId());
        assertEquals(TransactionType.SALE, emp1Txns.get(0).getType());

        List<Transaction> emp2Txns = dealership.getTransactionsByEmployee(emp2.getId());
        assertEquals(1, emp2Txns.size());
        assertEquals(emp2.getId(), emp2Txns.get(0).getEmployee().getId());
        assertEquals(TransactionType.RENTAL, emp2Txns.get(0).getType());

        // Empleado sin transacciones o parámetros inválidos
        assertTrue(dealership.getTransactionsByEmployee("EMP-999").isEmpty());
        assertTrue(dealership.getTransactionsByEmployee(null).isEmpty());
        assertTrue(dealership.getTransactionsByEmployee("  ").isEmpty());
    }

    @Test
    @DisplayName("registerTransaction debe permitir registrar transacciones y rechazar duplicados de ID (RF-19)")
    void testRegisterTransactionDuplicateRejection() throws Exception {
        SaleTransaction sale = new SaleTransaction("TXN-EXPLICIT-1", emp1, customer, sedan1, 48000.0, now);
        dealership.registerTransaction(sale);

        assertEquals(1, dealership.getAllTransactions().size());
        assertSame(sale, dealership.findTransactionById("TXN-EXPLICIT-1"));

        // Intentar registrar otra transacción con el mismo ID
        SaleTransaction duplicate = new SaleTransaction("TXN-EXPLICIT-1", emp2, customer, sedan1, 49000.0, now);
        assertThrows(DuplicateTransactionException.class, () ->
                dealership.registerTransaction(duplicate));
    }
}
