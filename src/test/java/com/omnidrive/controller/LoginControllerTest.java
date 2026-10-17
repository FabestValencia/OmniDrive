package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.model.Employee;
import com.omnidrive.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Controlador de Login (T3 - Spec 006)")
class LoginControllerTest {

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
    @DisplayName("Debe autenticar exitosamente al Administrador con credenciales por defecto (RF-07, Escenario 1)")
    void testSuccessfulAdminLogin() {
        LoginController controller = new LoginController();
        AtomicReference<User> loggedInUser = new AtomicReference<>();
        controller.setOnLoginSuccess(loggedInUser::set);

        boolean success = controller.processLogin("admin", "Admin123*");

        assertTrue(success, "El login de administrador debe ser exitoso");
        assertNotNull(loggedInUser.get(), "El callback de login debe recibir el usuario");
        assertEquals("ADMIN-001", loggedInUser.get().getId());
        assertTrue(AppContext.isAuthenticated());
        assertTrue(AppContext.isAdmin());
    }

    @Test
    @DisplayName("Debe rechazar credenciales incorrectas retornando false y mensaje de error")
    void testFailedLoginWithBadCredentials() {
        LoginController controller = new LoginController();
        AtomicBoolean navigated = new AtomicBoolean(false);
        controller.setOnLoginSuccess(u -> navigated.set(true));

        boolean success = controller.processLogin("admin", "ClaveIncorrecta");

        assertFalse(success, "El login debe fallar con clave errónea");
        assertFalse(navigated.get());
        assertFalse(AppContext.isAuthenticated());
        assertNotNull(controller.getLastErrorMessage());
        assertTrue(controller.getLastErrorMessage().toLowerCase().contains("credenciales") ||
                   controller.getLastErrorMessage().toLowerCase().contains("incorrectas"));
    }

    @Test
    @DisplayName("Debe rechazar autenticación ante empleado bloqueado (RF-12, Escenario 2)")
    void testBlockedEmployeeLoginRejected() throws Exception {
        Employee blocked = new Employee("EMP-888", "Mateo", "Gomez", "3008889900",
                "mateo@omnidrive.com", "mateo.gomez", "Clave1234*");
        AppContext.getDealership().registerEmployee(blocked);
        AppContext.getDealership().blockEmployee("EMP-888");

        LoginController controller = new LoginController();
        boolean success = controller.processLogin("mateo.gomez", "Clave1234*");

        assertFalse(success);
        assertFalse(AppContext.isAuthenticated());
        assertNotNull(controller.getLastErrorMessage());
        assertTrue(controller.getLastErrorMessage().toLowerCase().contains("bloqueada") ||
                   controller.getLastErrorMessage().toLowerCase().contains("bloqueado"));
    }

    @Test
    @DisplayName("Debe validar campos vacíos sin invocar autenticación")
    void testEmptyFieldsValidation() {
        LoginController controller = new LoginController();

        assertFalse(controller.processLogin("", ""));
        assertNotNull(controller.getLastErrorMessage());

        assertFalse(controller.processLogin("admin", ""));
        assertFalse(controller.processLogin("", "Admin123*"));
        assertFalse(AppContext.isAuthenticated());
    }
}
