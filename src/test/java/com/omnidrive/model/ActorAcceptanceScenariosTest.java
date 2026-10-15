package com.omnidrive.model;

import com.omnidrive.exception.BlockedUserException;
import com.omnidrive.exception.DuplicateUserException;
import com.omnidrive.exception.InvalidRecoveryCodeException;
import com.omnidrive.model.enums.EmployeeStatus;
import com.omnidrive.model.enums.UserRole;
import com.omnidrive.util.MockEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Escenarios de Aceptación Oficiales - Feature 002-actors-and-auth")
class ActorAcceptanceScenariosTest {

    private Dealership dealership;
    private MockEmailService emailService;

    @BeforeEach
    void setUp() {
        dealership = new Dealership("OmniDrive Principal");
        emailService = new MockEmailService();
        dealership.setEmailService(emailService);
    }

    @Test
    @DisplayName("Escenario 1: Autenticación exitosa de Empleado activo")
    void testScenario1_SuccessfulAuthenticationActiveEmployee() throws Exception {
        // Dado que existe un empleado activo con usuario "carlos.vendedor" y contraseña válida
        Employee employee = new Employee(
                "1094333444", "Carlos", "Restrepo", "3151112233",
                "carlos@omnidrive.com", "carlos.vendedor", "Ventas123*"
        );
        dealership.registerEmployee(employee);

        // Cuando el empleado suministra su usuario y contraseña correctos
        User authenticatedUser = dealership.authenticate("carlos.vendedor", "Ventas123*");

        // Entonces el sistema valida las credenciales y permite el acceso otorgando permisos de empleado
        assertNotNull(authenticatedUser);
        assertEquals("carlos.vendedor", authenticatedUser.getUsername());
        assertEquals(UserRole.EMPLOYEE, authenticatedUser.getRole());
        assertEquals(EmployeeStatus.ACTIVE, ((Employee) authenticatedUser).getStatus());
    }

    @Test
    @DisplayName("Escenario 2: Rechazo de autenticación para Empleado bloqueado")
    void testScenario2_RejectAuthenticationBlockedEmployee() throws Exception {
        // Dado que el Administrador ha marcado al empleado "laura.ventas" en estado "Bloqueado"
        Employee employee = new Employee(
                "1094555666", "Laura", "Castro", "3172223344",
                "laura@omnidrive.com", "laura.ventas", "ClaveSegura2026*"
        );
        dealership.registerEmployee(employee);
        dealership.blockEmployee("1094555666");

        // Cuando dicho empleado intenta iniciar sesión con su contraseña correcta
        // Entonces el sistema bloquea el ingreso e informa que la cuenta está deshabilitada
        BlockedUserException ex = assertThrows(BlockedUserException.class, () ->
                dealership.authenticate("laura.ventas", "ClaveSegura2026*")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("bloqueada"));
    }

    @Test
    @DisplayName("Escenario 3: Rechazo de registro por cédula o usuario duplicado")
    void testScenario3_RejectDuplicateIdOrUsername() throws Exception {
        // Dado que ya existe un empleado registrado con cédula "1094888999" y usuario "emp.juan"
        Employee initialEmp = new Employee(
                "1094888999", "Juan", "Ospina", "3104445566",
                "juan@omnidrive.com", "emp.juan", "Password123*"
        );
        dealership.registerEmployee(initialEmp);

        // Cuando se intenta registrar otro usuario con la misma cédula "1094888999"
        Employee duplicateIdEmp = new Employee(
                "1094888999", "Otro", "Nombre", "3119998877",
                "otro@omnidrive.com", "otro.user", "Password123*"
        );
        DuplicateUserException exId = assertThrows(DuplicateUserException.class, () ->
                dealership.registerEmployee(duplicateIdEmp)
        );
        assertTrue(exId.getMessage().contains("1094888999"));

        // O con el mismo nombre de usuario "emp.juan"
        Employee duplicateUserEmp = new Employee(
                "1094777111", "Tercero", "Apellido", "3128887766",
                "tercero@omnidrive.com", "emp.juan", "Password123*"
        );
        DuplicateUserException exUser = assertThrows(DuplicateUserException.class, () ->
                dealership.registerEmployee(duplicateUserEmp)
        );
        assertTrue(exUser.getMessage().contains("emp.juan"));
    }

    @Test
    @DisplayName("Escenario 4: Bloqueo permanente de empleado por parte del Administrador")
    void testScenario4_PermanentEmployeeBlock() throws Exception {
        // Dado un empleado activo "juan.perez" registrado en el concesionario
        Employee employee = new Employee(
                "1094000111", "Juan", "Perez", "3145556677",
                "juan.perez@omnidrive.com", "juan.perez", "ClaveOperativa1*"
        );
        dealership.registerEmployee(employee);
        assertFalse(dealership.findEmployeeById("1094000111").isBlocked());

        // Cuando el Administrador ejecuta la acción de bloqueo sobre su cuenta
        dealership.blockEmployee("1094000111");

        // Entonces el estado del empleado cambia a "Bloqueado", no existe mecanismo para reactivarlo
        // y futuras solicitudes de autenticación son rechazadas
        Employee blockedEmp = dealership.findEmployeeById("1094000111");
        assertTrue(blockedEmp.isBlocked());
        assertEquals(EmployeeStatus.BLOCKED, blockedEmp.getStatus());

        assertThrows(BlockedUserException.class, () ->
                dealership.authenticate("juan.perez", "ClaveOperativa1*")
        );
    }

    @Test
    @DisplayName("Escenario 5: Recuperación exitosa de contraseña vía correo electrónico dentro de los 15 minutos")
    void testScenario5_SuccessfulPasswordRecoveryWithin15Minutes() throws Exception {
        // Dado un usuario que olvidó su contraseña con correo registrado "admin@omnidrive.com"
        Instant t0 = Instant.parse("2026-10-15T12:00:00Z");

        // Cuando solicita la recuperación de clave y suministra el código de seguridad recibido en su correo
        // dentro de la ventana de 15 minutos junto con su nueva contraseña
        String code = dealership.requestPasswordRecovery("admin@omnidrive.com", t0);
        assertNotNull(code);
        assertEquals(1, emailService.getSentEmails().size());

        Instant t10Minutes = t0.plus(Duration.ofMinutes(10));
        dealership.resetPasswordWithCode("admin", code, "NuevaSuperClave2026*", t10Minutes);

        // Entonces el sistema actualiza la contraseña exitosamente permitiendo el inicio de sesión inmediato con la nueva clave
        User authenticatedAdmin = dealership.authenticate("admin", "NuevaSuperClave2026*");
        assertNotNull(authenticatedAdmin);
        assertEquals("admin", authenticatedAdmin.getUsername());
    }

    @Test
    @DisplayName("Escenario 6: Rechazo de código de recuperación expirado")
    void testScenario6_RejectExpiredRecoveryCode() throws Exception {
        // Dado que se generó un código de recuperación hace más de 15 minutos
        Instant t0 = Instant.parse("2026-10-15T12:00:00Z");
        String code = dealership.requestPasswordRecovery("admin@omnidrive.com", t0);

        // Cuando el usuario intenta restablecer la contraseña con dicho código expirado (+15 min y 1 seg)
        Instant tExpired = t0.plus(Duration.ofMinutes(15)).plusSeconds(1);

        // Entonces el sistema rechaza la solicitud e indica que el código ha caducado
        InvalidRecoveryCodeException ex = assertThrows(InvalidRecoveryCodeException.class, () ->
                dealership.resetPasswordWithCode("admin", code, "ClaveTardia*", tExpired)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("expirado"));
    }
}
