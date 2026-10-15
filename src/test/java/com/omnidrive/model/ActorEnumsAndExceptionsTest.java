package com.omnidrive.model;

import com.omnidrive.exception.*;
import com.omnidrive.model.enums.EmployeeStatus;
import com.omnidrive.model.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Enums y Excepciones del Dominio de Actores (T1)")
class ActorEnumsAndExceptionsTest {

    @Test
    @DisplayName("UserRole debe contener exactamente los roles ADMINISTRATOR, EMPLOYEE y CUSTOMER")
    void testUserRoleEnum() {
        assertEquals(3, UserRole.values().length);
        assertEquals(UserRole.ADMINISTRATOR, UserRole.valueOf("ADMINISTRATOR"));
        assertEquals(UserRole.EMPLOYEE, UserRole.valueOf("EMPLOYEE"));
        assertEquals(UserRole.CUSTOMER, UserRole.valueOf("CUSTOMER"));
    }

    @Test
    @DisplayName("EmployeeStatus debe contener exactamente ACTIVE y BLOCKED")
    void testEmployeeStatusEnum() {
        assertEquals(2, EmployeeStatus.values().length);
        assertEquals(EmployeeStatus.ACTIVE, EmployeeStatus.valueOf("ACTIVE"));
        assertEquals(EmployeeStatus.BLOCKED, EmployeeStatus.valueOf("BLOCKED"));
    }

    @Test
    @DisplayName("AuthenticationException debe heredar de OmniDriveException")
    void testAuthenticationException() {
        AuthenticationException ex = new AuthenticationException("Credenciales incorrectas.");
        assertTrue(ex instanceof OmniDriveException);
        assertEquals("Credenciales incorrectas.", ex.getMessage());
    }

    @Test
    @DisplayName("BlockedUserException debe heredar de AuthenticationException")
    void testBlockedUserException() {
        BlockedUserException ex = new BlockedUserException("Cuenta de empleado bloqueada.");
        assertTrue(ex instanceof AuthenticationException);
        assertEquals("Cuenta de empleado bloqueada.", ex.getMessage());
    }

    @Test
    @DisplayName("DuplicateUserException debe heredar de OmniDriveException")
    void testDuplicateUserException() {
        DuplicateUserException ex = new DuplicateUserException("1094888999", "La cédula ya existe.");
        assertTrue(ex instanceof OmniDriveException);
        assertEquals("1094888999", ex.getIdentifier());
        assertTrue(ex.getMessage().contains("1094888999"));
    }

    @Test
    @DisplayName("InvalidUserDataException debe heredar de OmniDriveException")
    void testInvalidUserDataException() {
        InvalidUserDataException ex = new InvalidUserDataException("El correo electrónico no es válido.");
        assertTrue(ex instanceof OmniDriveException);
        assertEquals("El correo electrónico no es válido.", ex.getMessage());
    }

    @Test
    @DisplayName("InvalidRecoveryCodeException debe heredar de OmniDriveException")
    void testInvalidRecoveryCodeException() {
        InvalidRecoveryCodeException ex = new InvalidRecoveryCodeException("El código de recuperación ha expirado.");
        assertTrue(ex instanceof OmniDriveException);
        assertEquals("El código de recuperación ha expirado.", ex.getMessage());
    }
}
