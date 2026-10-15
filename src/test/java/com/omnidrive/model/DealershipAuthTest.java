package com.omnidrive.model;

import com.omnidrive.exception.AuthenticationException;
import com.omnidrive.exception.BlockedUserException;
import com.omnidrive.exception.InvalidRecoveryCodeException;
import com.omnidrive.exception.InvalidUserDataException;
import com.omnidrive.model.enums.UserRole;
import com.omnidrive.util.MockEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Autenticación y Recuperación de Clave en Dealership (T6)")
class DealershipAuthTest {

    private Dealership dealership;
    private MockEmailService mockEmailService;

    @BeforeEach
    void setUp() throws Exception {
        dealership = new Dealership("OmniDrive Central");
        mockEmailService = new MockEmailService();
        dealership.setEmailService(mockEmailService);

        // Registrar empleado de prueba
        Employee emp = new Employee("1094111222", "Carlos", "Ventas", "3123456789", "carlos@omnidrive.com", "carlos.vendedor", "Ventas2026*");
        dealership.registerEmployee(emp);
    }

    @Test
    @DisplayName("Debe autenticar exitosamente al Administrador raíz con credenciales correctas (RF-08, RF-20)")
    void testAuthenticateAdministratorSuccessfully() throws Exception {
        User user = dealership.authenticate("admin", "Admin123*");

        assertNotNull(user);
        assertEquals("admin", user.getUsername());
        assertEquals(UserRole.ADMINISTRATOR, user.getRole());
    }

    @Test
    @DisplayName("Debe autenticar exitosamente a un Empleado activo con credenciales correctas (RF-08)")
    void testAuthenticateEmployeeSuccessfully() throws Exception {
        User user = dealership.authenticate("carlos.vendedor", "Ventas2026*");

        assertNotNull(user);
        assertEquals("carlos.vendedor", user.getUsername());
        assertEquals(UserRole.EMPLOYEE, user.getRole());
    }

    @Test
    @DisplayName("Debe rechazar credenciales incorrectas para usuario existente (RF-15)")
    void testRejectWrongPassword() {
        assertThrows(AuthenticationException.class, () ->
                dealership.authenticate("carlos.vendedor", "PasswordErroneo")
        );
    }

    @Test
    @DisplayName("Debe rechazar autenticación para usuario inexistente (RF-15)")
    void testRejectNonExistentUser() {
        assertThrows(AuthenticationException.class, () ->
                dealership.authenticate("usuario.inexistente", "Cualquiera123*")
        );
    }

    @Test
    @DisplayName("Debe rechazar con BlockedUserException a un empleado bloqueado (RF-06, RF-16)")
    void testRejectBlockedEmployeeAuthentication() throws Exception {
        dealership.blockEmployee("1094111222");

        BlockedUserException ex = assertThrows(BlockedUserException.class, () ->
                dealership.authenticate("carlos.vendedor", "Ventas2026*")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("bloqueada"));
    }

    @Test
    @DisplayName("Debe generar código de recuperación y enviar correo electrónico (RF-13)")
    void testRequestPasswordRecovery() throws Exception {
        Instant now = Instant.parse("2026-10-15T10:00:00Z");

        String code = dealership.requestPasswordRecovery("carlos.vendedor", now);

        assertNotNull(code);
        assertFalse(code.isEmpty());
        assertEquals(1, mockEmailService.getSentEmails().size());
        MockEmailService.SentEmail sent = mockEmailService.getSentEmails().get(0);
        assertEquals("carlos@omnidrive.com", sent.to());
        assertTrue(sent.body().contains(code));
    }

    @Test
    @DisplayName("Debe permitir restablecer contraseña con código válido dentro de 15 minutos (RF-14)")
    void testResetPasswordWithValidCode() throws Exception {
        Instant now = Instant.parse("2026-10-15T10:00:00Z");
        String code = dealership.requestPasswordRecovery("carlos@omnidrive.com", now);

        Instant at10Minutes = now.plus(Duration.ofMinutes(10));
        dealership.resetPasswordWithCode("carlos.vendedor", code, "NuevaPassword2026*", at10Minutes);

        // Debe poder iniciar sesión con la nueva contraseña
        User user = dealership.authenticate("carlos.vendedor", "NuevaPassword2026*");
        assertNotNull(user);

        // La contraseña anterior ya no es válida
        assertThrows(AuthenticationException.class, () ->
                dealership.authenticate("carlos.vendedor", "Ventas2026*")
        );
    }

    @Test
    @DisplayName("Debe rechazar restablecimiento con código expirado después de 15 minutos (RF-19)")
    void testRejectExpiredRecoveryCode() throws Exception {
        Instant now = Instant.parse("2026-10-15T10:00:00Z");
        String code = dealership.requestPasswordRecovery("carlos.vendedor", now);

        Instant after15Minutes = now.plus(Duration.ofMinutes(15)).plusSeconds(1);

        InvalidRecoveryCodeException ex = assertThrows(InvalidRecoveryCodeException.class, () ->
                dealership.resetPasswordWithCode("carlos.vendedor", code, "NuevaPassword*", after15Minutes)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("expirado"));
    }

    @Test
    @DisplayName("Debe rechazar código de recuperación incorrecto (RF-19)")
    void testRejectWrongRecoveryCode() throws Exception {
        Instant now = Instant.parse("2026-10-15T10:00:00Z");
        dealership.requestPasswordRecovery("carlos.vendedor", now);

        assertThrows(InvalidRecoveryCodeException.class, () ->
                dealership.resetPasswordWithCode("carlos.vendedor", "CODIGO_FALSO", "NuevaPassword*", now.plus(Duration.ofMinutes(5)))
        );
    }
}
