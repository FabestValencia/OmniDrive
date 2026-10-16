package com.omnidrive.model;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Jerarquía de Subclases Transaccionales (T3)")
class TransactionHierarchyTest {

    private Employee employee;
    private Customer customer;
    private Sedan newSedan;
    private Sedan usedSedanWithReview;
    private Sedan usedSedanWithoutReview;
    private Instant now;

    @BeforeEach
    void setUp() throws Exception {
        employee = new Employee("EMP-001", "Carlos", "Perez", "3001234567",
                "carlos@omnidrive.com", "carlos.perez", "SecurePass123!");
        customer = new Customer("CUST-001", "Maria", "Gomez", "3119876543",
                "maria@gmail.com");

        newSedan = new Sedan("NEW-001", "Toyota", 2024, 6, 200.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 50000.0, "toyota.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                450.0, true, true, true, true);

        usedSedanWithReview = new Sedan("USD-001", "Mazda", 2020, 6, 190.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.USED,
                VehicleStatus.AVAILABLE_FOR_SALE, 30000.0, "mazda.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                420.0, true, true, false, false);

        // Usamos una subclase anónima con isTechnicalReviewApproved() false para probar la regla en PurchaseTransaction
        usedSedanWithoutReview = new Sedan("USD-002", "Chevrolet", 2019, 5, 180.0, 1600.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.USED,
                VehicleStatus.AVAILABLE_FOR_SALE, 25000.0, "chevy.jpg",
                true, null, null,
                5, 4, 4, true, false, false,
                390.0, false, false, false, false) {
            @Override
            public boolean isTechnicalReviewApproved() {
                return false;
            }
        };

        now = Instant.now();
    }

    // --- PURCHASE TRANSACTION TESTS ---

    @Test
    @DisplayName("PurchaseTransaction debe crearse exitosamente para vehículo nuevo o usado con revisión técnica")
    void testPurchaseTransactionCreation() throws Exception {
        PurchaseTransaction purchaseNew = new PurchaseTransaction("TXN-P001", employee, customer,
                newSedan, 45000.0, now, VehicleStatus.AVAILABLE_FOR_SALE);

        assertEquals("TXN-P001", purchaseNew.getId());
        assertEquals(TransactionType.PURCHASE, purchaseNew.getType());
        assertEquals(VehicleStatus.AVAILABLE_FOR_SALE, purchaseNew.getInitialInventoryStatus());

        PurchaseTransaction purchaseUsed = new PurchaseTransaction("TXN-P002", employee, customer,
                usedSedanWithReview, 27000.0, now, VehicleStatus.AVAILABLE_FOR_RENT);
        assertEquals(VehicleStatus.AVAILABLE_FOR_RENT, purchaseUsed.getInitialInventoryStatus());
    }

    @Test
    @DisplayName("PurchaseTransaction debe rechazar compra de vehículo usado sin revisión técnica aprobada (RF-09, RF-14)")
    void testPurchaseTransactionRejectsUsedWithoutReview() {
        assertThrows(TechnicalReviewRejectedException.class, () ->
                new PurchaseTransaction("TXN-P003", employee, customer,
                        usedSedanWithoutReview, 20000.0, now, VehicleStatus.AVAILABLE_FOR_SALE));
    }

    @Test
    @DisplayName("PurchaseTransaction debe rechazar estado inicial inválido (debe ser AVAILABLE_FOR_SALE o AVAILABLE_FOR_RENT)")
    void testPurchaseTransactionRejectsInvalidInitialStatus() {
        assertThrows(InvalidTransactionException.class, () ->
                new PurchaseTransaction("TXN-P004", employee, customer,
                        newSedan, 45000.0, now, VehicleStatus.SOLD));
        assertThrows(InvalidTransactionException.class, () ->
                new PurchaseTransaction("TXN-P005", employee, customer,
                        newSedan, 45000.0, now, null));
    }

    // --- SALE TRANSACTION TESTS ---

    @Test
    @DisplayName("SaleTransaction debe crearse exitosamente para vehículo disponible para venta (RF-11)")
    void testSaleTransactionCreation() throws Exception {
        SaleTransaction sale = new SaleTransaction("TXN-S001", employee, customer, newSedan, 50000.0, now);
        assertEquals("TXN-S001", sale.getId());
        assertEquals(TransactionType.SALE, sale.getType());
        assertEquals(50000.0, sale.getTotalAmount(), 0.001);
    }

    @Test
    @DisplayName("SaleTransaction debe rechazar vehículo que no esté en estado AVAILABLE_FOR_SALE (RF-07, RF-16)")
    void testSaleTransactionRejectsUnavailableVehicle() throws Exception {
        newSedan.setStatus(VehicleStatus.AVAILABLE_FOR_RENT);
        assertThrows(VehicleUnavailableException.class, () ->
                new SaleTransaction("TXN-S002", employee, customer, newSedan, 50000.0, now));
    }

    // --- RENTAL TRANSACTION TESTS ---

    @Test
    @DisplayName("RentalTransaction debe crearse exitosamente calculando totalAmount = días * tarifaBase (RF-05, RF-12)")
    void testRentalTransactionCreation() throws Exception {
        newSedan.setStatus(VehicleStatus.AVAILABLE_FOR_RENT);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(5);
        int rentalDays = 5;
        double expectedTotal = rentalDays * newSedan.getBasePrice();

        RentalTransaction rental = new RentalTransaction("TXN-R001", employee, customer,
                newSedan, now, start, end, rentalDays);

        assertEquals("TXN-R001", rental.getId());
        assertEquals(TransactionType.RENTAL, rental.getType());
        assertEquals(start, rental.getStartDate());
        assertEquals(end, rental.getEndDate());
        assertEquals(rentalDays, rental.getRentalDays());
        assertEquals(expectedTotal, rental.getTotalAmount(), 0.001);
    }

    @Test
    @DisplayName("RentalTransaction debe rechazar vehículo que no esté en estado AVAILABLE_FOR_RENT (RF-08, RF-17)")
    void testRentalTransactionRejectsUnavailableVehicle() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);
        // newSedan está en AVAILABLE_FOR_SALE
        assertThrows(VehicleUnavailableException.class, () ->
                new RentalTransaction("TXN-R002", employee, customer, newSedan, now, start, end, 3));
    }

    @Test
    @DisplayName("RentalTransaction debe rechazar inconsistencias de fechas o días no positivos (RF-18)")
    void testRentalTransactionRejectsInvalidDates() {
        newSedan.setStatus(VehicleStatus.AVAILABLE_FOR_RENT);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);

        // start después de end
        assertThrows(InvalidTransactionException.class, () ->
                new RentalTransaction("TXN-R003", employee, customer, newSedan, now, end, start, 3));

        // fechas nulas
        assertThrows(InvalidTransactionException.class, () ->
                new RentalTransaction("TXN-R004", employee, customer, newSedan, now, null, end, 3));
        assertThrows(InvalidTransactionException.class, () ->
                new RentalTransaction("TXN-R005", employee, customer, newSedan, now, start, null, 3));

        // días <= 0
        assertThrows(InvalidTransactionException.class, () ->
                new RentalTransaction("TXN-R006", employee, customer, newSedan, now, start, end, 0));
        assertThrows(InvalidTransactionException.class, () ->
                new RentalTransaction("TXN-R007", employee, customer, newSedan, now, start, end, -2));
    }

    @Test
    @DisplayName("Las tres transacciones deben ser serializables")
    void testSubclassesSerialization() throws Exception {
        newSedan.setStatus(VehicleStatus.AVAILABLE_FOR_RENT);
        PurchaseTransaction purchase = new PurchaseTransaction("TXN-P010", employee, customer,
                newSedan, 45000.0, now, VehicleStatus.AVAILABLE_FOR_SALE);
        SaleTransaction sale = new SaleTransaction("TXN-S010", employee, customer,
                usedSedanWithReview, 30000.0, now);
        RentalTransaction rental = new RentalTransaction("TXN-R010", employee, customer,
                newSedan, now, LocalDate.now(), LocalDate.now().plusDays(2), 2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(purchase);
            oos.writeObject(sale);
            oos.writeObject(rental);
        }

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            Object pObj = ois.readObject();
            Object sObj = ois.readObject();
            Object rObj = ois.readObject();

            assertTrue(pObj instanceof PurchaseTransaction);
            assertTrue(sObj instanceof SaleTransaction);
            assertTrue(rObj instanceof RentalTransaction);
        }
    }
}
