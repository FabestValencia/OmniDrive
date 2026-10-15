package com.omnidrive.model;

import com.omnidrive.exception.InvalidUserDataException;
import com.omnidrive.model.enums.EmployeeStatus;
import com.omnidrive.model.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de la Jerarquía de Usuarios y Empleados (T3)")
class UserHierarchyTest {

    @Test
    @DisplayName("Debe crear un Employee válido con rol EMPLOYEE y estado inicial ACTIVE")
    void testValidEmployeeCreation() throws InvalidUserDataException {
        Employee emp = new Employee("1094111222", "Laura", "Gomez", "3123456789", "laura@omnidrive.com", "laura.ventas", "Password123");

        assertEquals("1094111222", emp.getId());
        assertEquals("laura.ventas", emp.getUsername());
        assertEquals(UserRole.EMPLOYEE, emp.getRole());
        assertEquals(EmployeeStatus.ACTIVE, emp.getStatus());
        assertFalse(emp.isBlocked());
        assertTrue(emp.verifyPassword("Password123"));
        assertFalse(emp.verifyPassword("WrongPassword"));
    }

    @Test
    @DisplayName("Debe crear un Administrator válido con rol ADMINISTRATOR")
    void testValidAdministratorCreation() throws InvalidUserDataException {
        Administrator admin = new Administrator("1000000001", "Admin", "General", "3000000000", "admin@omnidrive.com", "admin", "Admin2026*");

        assertEquals("admin", admin.getUsername());
        assertEquals(UserRole.ADMINISTRATOR, admin.getRole());
        assertTrue(admin.verifyPassword("Admin2026*"));
    }

    @Test
    @DisplayName("El bloqueo de un empleado debe ser permanente y reflejar status BLOCKED")
    void testEmployeeBlock() throws InvalidUserDataException {
        Employee emp = new Employee("1094111222", "Laura", "Gomez", "3123456789", "laura@omnidrive.com", "laura.ventas", "Password123");
        assertFalse(emp.isBlocked());

        emp.block();

        assertTrue(emp.isBlocked());
        assertEquals(EmployeeStatus.BLOCKED, emp.getStatus());
    }

    @Test
    @DisplayName("Debe rechazar username nulo, vacío o en blanco")
    void testRejectInvalidUsername() {
        List<String> invalidUsernames = List.of("", "   ");
        for (String invalidUser : invalidUsernames) {
            InvalidUserDataException ex = assertThrows(InvalidUserDataException.class, () ->
                    new Employee("1094111222", "Laura", "Gomez", "3123456789", "laura@omnidrive.com", invalidUser, "Password123")
            );
            assertTrue(ex.getMessage().toLowerCase().contains("usuario"));
        }

        assertThrows(InvalidUserDataException.class, () ->
                new Employee("1094111222", "Laura", "Gomez", "3123456789", "laura@omnidrive.com", null, "Password123")
        );
    }

    @Test
    @DisplayName("Debe rechazar contraseña nula, vacía o en blanco")
    void testRejectInvalidPassword() {
        List<String> invalidPasswords = List.of("", "   ");
        for (String invalidPass : invalidPasswords) {
            InvalidUserDataException ex = assertThrows(InvalidUserDataException.class, () ->
                    new Employee("1094111222", "Laura", "Gomez", "3123456789", "laura@omnidrive.com", "laura.ventas", invalidPass)
            );
            assertTrue(ex.getMessage().toLowerCase().contains("contraseña"));
        }

        assertThrows(InvalidUserDataException.class, () ->
                new Employee("1094111222", "Laura", "Gomez", "3123456789", "laura@omnidrive.com", "laura.ventas", null)
        );
    }

    @Test
    @DisplayName("Debe permitir cambiar la contraseña válidamente")
    void testChangePassword() throws InvalidUserDataException {
        Employee emp = new Employee("1094111222", "Laura", "Gomez", "3123456789", "laura@omnidrive.com", "laura.ventas", "OldPass123");
        assertTrue(emp.verifyPassword("OldPass123"));

        emp.changePassword("NewSecret456");

        assertTrue(emp.verifyPassword("NewSecret456"));
        assertFalse(emp.verifyPassword("OldPass123"));
    }

    @Test
    @DisplayName("No debe almacenar la contraseña en texto plano")
    void testPasswordNotStoredInPlainText() throws InvalidUserDataException {
        Employee emp = new Employee("1094111222", "Laura", "Gomez", "3123456789", "laura@omnidrive.com", "laura.ventas", "SecretPassword!");
        assertFalse(emp.toString().contains("SecretPassword!"));
    }
}
