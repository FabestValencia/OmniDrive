package com.omnidrive.util;

import com.omnidrive.exception.OmniDriveException;
import com.omnidrive.exception.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Excepción PersistenceException (T1)")
class PersistenceExceptionTest {

    @Test
    @DisplayName("Debe heredar de OmniDriveException y soportar mensaje y causa")
    void testPersistenceExceptionConstructors() {
        PersistenceException ex1 = new PersistenceException("Error al acceder al archivo de persistencia");
        assertTrue(ex1 instanceof OmniDriveException);
        assertEquals("Error al acceder al archivo de persistencia", ex1.getMessage());
        assertNull(ex1.getCause());

        IOException cause = new IOException("Disco lleno o archivo bloqueado");
        PersistenceException ex2 = new PersistenceException("Fallo de E/S en persistencia", cause);
        assertEquals("Fallo de E/S en persistencia", ex2.getMessage());
        assertSame(cause, ex2.getCause());
    }

    @Test
    @DisplayName("PersistenceException debe ser serializable con serialVersionUID")
    void testSerialization() throws Exception {
        PersistenceException ex = new PersistenceException("Fallo en serialización");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(ex);
        }

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            Object deserialized = ois.readObject();
            assertTrue(deserialized instanceof PersistenceException);
            assertEquals(ex.getMessage(), ((PersistenceException) deserialized).getMessage());
        }
    }
}
