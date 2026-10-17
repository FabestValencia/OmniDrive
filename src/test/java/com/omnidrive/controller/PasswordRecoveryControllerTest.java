package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Controlador de Recuperación de Claves (T4 - Spec 006)")
class PasswordRecoveryControllerTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit ya inicializado
        }
    }

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        AppContext.reset();
        AppContext.init(tempDir.resolve("dealership.dat"));
    }

    @Test
    @DisplayName("Debe completar el flujo exitoso de solicitud de código y restablecimiento de contraseña (RF-08, Escenario 3)")
    void testSuccessfulPasswordRecoveryWorkflow() {
        PasswordRecoveryController controller = new PasswordRecoveryController();
        Instant now = Instant.parse("2026-10-16T10:00:00Z");

        // Paso 1: Solicitud de código
        boolean requested = controller.processCodeRequest("admin", now);
        assertTrue(requested, "La solicitud de código debe ser exitosa");
        String code = controller.getGeneratedCode();
        assertNotNull(code, "Debe tener un código de verificación de 6 dígitos");
        assertEquals(6, code.length());

        // Paso 2: Reseteo de contraseña dentro de los 15 minutos
        AtomicBoolean successNotified = new AtomicBoolean(false);
        controller.setOnSuccessCallback(() -> successNotified.set(true));

        boolean reset = controller.processPasswordReset("admin", code, "NuevoAdminPass2026*", now.plusSeconds(300));
        assertTrue(reset, "El reseteo con código válido y en tiempo debe ser exitoso");
        assertTrue(successNotified.get());

        // Verificar autenticación con la nueva credencial
        assertDoesNotThrow(() ->
                AppContext.getDealership().authenticate("admin", "NuevoAdminPass2026*")
        );
    }

    @Test
    @DisplayName("Debe rechazar solicitud de código ante usuario inexistente")
    void testRequestCodeFailsForNonExistentUser() {
        PasswordRecoveryController controller = new PasswordRecoveryController();
        Instant now = Instant.now();

        boolean requested = controller.processCodeRequest("no.existe@omnidrive.com", now);
        assertFalse(requested);
        assertNotNull(controller.getLastErrorMessage());
        assertTrue(controller.getLastErrorMessage().toLowerCase().contains("no se encontró") ||
                   controller.getLastErrorMessage().toLowerCase().contains("no existe") ||
                   controller.getLastErrorMessage().toLowerCase().contains("error"));
    }

    @Test
    @DisplayName("Debe rechazar el restablecimiento si el código es incorrecto")
    void testResetPasswordFailsWithWrongCode() {
        PasswordRecoveryController controller = new PasswordRecoveryController();
        Instant now = Instant.now();
        controller.processCodeRequest("admin", now);

        boolean reset = controller.processPasswordReset("admin", "000000", "ClaveInvalida1*", now.plusSeconds(60));
        assertFalse(reset);
        assertNotNull(controller.getLastErrorMessage());
        assertTrue(controller.getLastErrorMessage().toLowerCase().contains("incorrecto") ||
                   controller.getLastErrorMessage().toLowerCase().contains("inválido") ||
                   controller.getLastErrorMessage().toLowerCase().contains("invalido"));
    }

    @Test
    @DisplayName("Debe rechazar el restablecimiento si el código ha expirado (> 15 minutos)")
    void testResetPasswordFailsWhenCodeExpired() {
        PasswordRecoveryController controller = new PasswordRecoveryController();
        Instant start = Instant.parse("2026-10-16T12:00:00Z");
        controller.processCodeRequest("admin", start);
        String code = controller.getGeneratedCode();

        // 16 minutos después (expirado)
        Instant expiredTime = start.plusSeconds(16 * 60);
        boolean reset = controller.processPasswordReset("admin", code, "ClaveExpirada1*", expiredTime);

        assertFalse(reset);
        assertNotNull(controller.getLastErrorMessage());
        assertTrue(controller.getLastErrorMessage().toLowerCase().contains("expirado") ||
                   controller.getLastErrorMessage().toLowerCase().contains("expiró"));
    }
}
