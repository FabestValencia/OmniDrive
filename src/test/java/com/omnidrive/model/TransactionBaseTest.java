package com.omnidrive.model;

import com.omnidrive.exception.InvalidTransactionException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransactionType;
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
import java.io.Serial;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Clase Abstracta Transaction (T2)")
class TransactionBaseTest {

    private Employee employee;
    private Customer customer;
    private Vehicle vehicle;
    private Instant now;

    // Subclase concreta para probar la clase abstracta Transaction
    private static class ConcreteTransaction extends Transaction {
        @Serial
        private static final long serialVersionUID = 1L;

        public ConcreteTransaction(String id, TransactionType type, Employee employee,
                                   Customer customer, Vehicle vehicle, double totalAmount, Instant timestamp)
                throws InvalidTransactionException {
            super(id, type, employee, customer, vehicle, totalAmount, timestamp);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        employee = new Employee("EMP-001", "Carlos", "Perez", "3001234567",
                "carlos@omnidrive.com", "carlos.perez", "SecurePass123!");
        customer = new Customer("CUST-001", "Maria", "Gomez", "3119876543",
                "maria@gmail.com");
        vehicle = new Sedan(
                "ABC-123", "Toyota", 2024, 6, 200.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 50000.0, "toyota.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                450.0, true, true, true, true
        );
        now = Instant.now();
    }

    @Test
    @DisplayName("Debe instanciar correctamente con todos los campos válidos")
    void testValidTransactionInstantiation() throws Exception {
        Transaction txn = new ConcreteTransaction("TXN-00001", TransactionType.SALE,
                employee, customer, vehicle, 50000.0, now);

        assertEquals("TXN-00001", txn.getId());
        assertEquals(TransactionType.SALE, txn.getType());
        assertSame(employee, txn.getEmployee());
        assertSame(customer, txn.getCustomer());
        assertSame(vehicle, txn.getVehicle());
        assertEquals(50000.0, txn.getTotalAmount(), 0.001);
        assertEquals(now, txn.getTimestamp());
    }

    @Test
    @DisplayName("Debe rechazar ID nulo o vacío")
    void testRejectNullOrBlankId() {
        assertThrows(InvalidTransactionException.class, () ->
                new ConcreteTransaction(null, TransactionType.SALE, employee, customer, vehicle, 50000.0, now));
        assertThrows(InvalidTransactionException.class, () ->
                new ConcreteTransaction("   ", TransactionType.SALE, employee, customer, vehicle, 50000.0, now));
    }

    @Test
    @DisplayName("Debe rechazar tipo de transacción nulo")
    void testRejectNullTransactionType() {
        assertThrows(InvalidTransactionException.class, () ->
                new ConcreteTransaction("TXN-00001", null, employee, customer, vehicle, 50000.0, now));
    }

    @Test
    @DisplayName("Debe rechazar referencias nulas a empleado, cliente o vehículo")
    void testRejectNullReferences() {
        assertThrows(InvalidTransactionException.class, () ->
                new ConcreteTransaction("TXN-00001", TransactionType.SALE, null, customer, vehicle, 50000.0, now));
        assertThrows(InvalidTransactionException.class, () ->
                new ConcreteTransaction("TXN-00001", TransactionType.SALE, employee, null, vehicle, 50000.0, now));
        assertThrows(InvalidTransactionException.class, () ->
                new ConcreteTransaction("TXN-00001", TransactionType.SALE, employee, customer, null, 50000.0, now));
    }

    @Test
    @DisplayName("Debe rechazar monto menor o igual a cero (RF-04)")
    void testRejectZeroOrNegativeAmount() {
        assertThrows(InvalidTransactionException.class, () ->
                new ConcreteTransaction("TXN-00001", TransactionType.SALE, employee, customer, vehicle, 0.0, now));
        assertThrows(InvalidTransactionException.class, () ->
                new ConcreteTransaction("TXN-00001", TransactionType.SALE, employee, customer, vehicle, -100.0, now));
    }

    @Test
    @DisplayName("Debe rechazar timestamp de auditoría nulo")
    void testRejectNullTimestamp() {
        assertThrows(InvalidTransactionException.class, () ->
                new ConcreteTransaction("TXN-00001", TransactionType.SALE, employee, customer, vehicle, 50000.0, null));
    }

    @Test
    @DisplayName("Transaction debe ser serializable con serialVersionUID")
    void testSerialization() throws Exception {
        Transaction txn = new ConcreteTransaction("TXN-00001", TransactionType.PURCHASE,
                employee, customer, vehicle, 35000.0, now);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(txn);
        }

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            Object deserialized = ois.readObject();
            assertTrue(deserialized instanceof Transaction);
            Transaction deserializedTxn = (Transaction) deserialized;
            assertEquals(txn.getId(), deserializedTxn.getId());
            assertEquals(txn.getType(), deserializedTxn.getType());
            assertEquals(txn.getEmployee().getId(), deserializedTxn.getEmployee().getId());
            assertEquals(txn.getCustomer().getId(), deserializedTxn.getCustomer().getId());
            assertEquals(txn.getVehicle().getPlate(), deserializedTxn.getVehicle().getPlate());
            assertEquals(txn.getTotalAmount(), deserializedTxn.getTotalAmount(), 0.001);
            assertEquals(txn.getTimestamp(), deserializedTxn.getTimestamp());
        }
    }
}
