package com.omnidrive.model;

import com.omnidrive.exception.DuplicateTransactionException;
import com.omnidrive.exception.InvalidTransactionException;
import com.omnidrive.exception.OmniDriveException;
import com.omnidrive.exception.VehicleUnavailableException;
import com.omnidrive.model.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Enums y Excepciones del Módulo Transaccional (T1)")
class TransactionEnumsAndExceptionsTest {

    @Test
    @DisplayName("TransactionType debe contener los tipos PURCHASE, SALE y RENTAL")
    void testTransactionTypeValues() {
        TransactionType[] types = TransactionType.values();
        assertEquals(3, types.length);
        assertEquals(TransactionType.PURCHASE, TransactionType.valueOf("PURCHASE"));
        assertEquals(TransactionType.SALE, TransactionType.valueOf("SALE"));
        assertEquals(TransactionType.RENTAL, TransactionType.valueOf("RENTAL"));
    }

    @Test
    @DisplayName("VehicleUnavailableException debe heredar de OmniDriveException y ser serializable")
    void testVehicleUnavailableException() throws Exception {
        VehicleUnavailableException ex = new VehicleUnavailableException("Vehículo no disponible para la operación");
        assertTrue(ex instanceof OmniDriveException);
        assertEquals("Vehículo no disponible para la operación", ex.getMessage());

        // Test de serialización
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(ex);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            Object deserialized = ois.readObject();
            assertTrue(deserialized instanceof VehicleUnavailableException);
            assertEquals(ex.getMessage(), ((VehicleUnavailableException) deserialized).getMessage());
        }
    }

    @Test
    @DisplayName("InvalidTransactionException debe heredar de OmniDriveException y ser serializable")
    void testInvalidTransactionException() throws Exception {
        InvalidTransactionException ex = new InvalidTransactionException("Datos inválidos en la transacción");
        assertTrue(ex instanceof OmniDriveException);
        assertEquals("Datos inválidos en la transacción", ex.getMessage());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(ex);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            Object deserialized = ois.readObject();
            assertTrue(deserialized instanceof InvalidTransactionException);
            assertEquals(ex.getMessage(), ((InvalidTransactionException) deserialized).getMessage());
        }
    }

    @Test
    @DisplayName("DuplicateTransactionException debe heredar de OmniDriveException y ser serializable")
    void testDuplicateTransactionException() throws Exception {
        DuplicateTransactionException ex = new DuplicateTransactionException("Transacción duplicada: TXN-00001");
        assertTrue(ex instanceof OmniDriveException);
        assertEquals("Transacción duplicada: TXN-00001", ex.getMessage());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(ex);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            Object deserialized = ois.readObject();
            assertTrue(deserialized instanceof DuplicateTransactionException);
            assertEquals(ex.getMessage(), ((DuplicateTransactionException) deserialized).getMessage());
        }
    }
}
