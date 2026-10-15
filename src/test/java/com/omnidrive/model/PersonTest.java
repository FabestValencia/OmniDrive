package com.omnidrive.model;

import com.omnidrive.exception.InvalidUserDataException;
import com.omnidrive.model.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Person y Customer (T2)")
class PersonTest {

    @Test
    @DisplayName("Debe crear un Customer válido con datos correctos y rol CUSTOMER")
    void testValidCustomerCreation() throws InvalidUserDataException {
        Customer customer = new Customer("1094888999", "Carlos", "Perez", "3101234567", "carlos.perez@example.com");

        assertEquals("1094888999", customer.getId());
        assertEquals("Carlos", customer.getFirstName());
        assertEquals("Perez", customer.getLastName());
        assertEquals("3101234567", customer.getPhone());
        assertEquals("carlos.perez@example.com", customer.getEmail());
        assertEquals(UserRole.CUSTOMER, customer.getRole());
    }

    @Test
    @DisplayName("Debe rechazar cédula vacía, nula o en blanco")
    void testRejectBlankId() {
        List<String> invalidIds = List.of("", "   ");
        for (String blankId : invalidIds) {
            InvalidUserDataException ex = assertThrows(InvalidUserDataException.class, () ->
                    new Customer(blankId, "Carlos", "Perez", "3101234567", "carlos@example.com")
            );
            assertTrue(ex.getMessage().toLowerCase().contains("cédula") || ex.getMessage().toLowerCase().contains("identificación"));
        }

        assertThrows(InvalidUserDataException.class, () ->
                new Customer(null, "Carlos", "Perez", "3101234567", "carlos@example.com")
        );
    }

    @Test
    @DisplayName("Debe rechazar nombres o apellidos vacíos")
    void testRejectBlankNames() {
        List<String> blankNames = List.of("", "   ");
        for (String blankName : blankNames) {
            assertThrows(InvalidUserDataException.class, () ->
                    new Customer("123", blankName, "Perez", "3101234567", "carlos@example.com")
            );
            assertThrows(InvalidUserDataException.class, () ->
                    new Customer("123", "Carlos", blankName, "3101234567", "carlos@example.com")
            );
        }
    }

    @Test
    @DisplayName("Debe rechazar formatos de correo electrónico inválidos")
    void testRejectInvalidEmailFormats() {
        List<String> invalidEmails = List.of("sin-arroba", "correo@", "@dominio.com", "invalido@com", "espacios en@correo.com");
        for (String invalidEmail : invalidEmails) {
            InvalidUserDataException ex = assertThrows(InvalidUserDataException.class, () ->
                    new Customer("123", "Carlos", "Perez", "3101234567", invalidEmail)
            );
            assertTrue(ex.getMessage().toLowerCase().contains("correo"));
        }
    }

    @Test
    @DisplayName("Debe normalizar email a minúsculas y recortar espacios en campos")
    void testDataNormalization() throws InvalidUserDataException {
        Customer customer = new Customer("  1094888999  ", "  Carlos  ", "  Perez  ", "  3101234567  ", "  CARLOS.PEREZ@EXAMPLE.COM  ");

        assertEquals("1094888999", customer.getId());
        assertEquals("Carlos", customer.getFirstName());
        assertEquals("Perez", customer.getLastName());
        assertEquals("3101234567", customer.getPhone());
        assertEquals("carlos.perez@example.com", customer.getEmail());
    }

    @Test
    @DisplayName("Dos personas con la misma cédula deben ser iguales (equals y hashCode)")
    void testEqualsAndHashCode() throws InvalidUserDataException {
        Customer c1 = new Customer("1094888999", "Carlos", "Perez", "3101234567", "carlos@example.com");
        Customer c2 = new Customer("1094888999", "Otro", "Nombre", "3009876543", "otro@example.com");

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }
}
