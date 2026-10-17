package com.omnidrive.context;

import com.omnidrive.model.Dealership;
import com.omnidrive.model.Employee;
import com.omnidrive.model.User;
import com.omnidrive.util.DealershipPersistenceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - AppContext y Gestión de Sesión (T1 - Spec 006)")
class AppContextTest {

    @BeforeEach
    void resetContext() {
        AppContext.reset();
    }

    @Test
    @DisplayName("Debe inicializar AppContext con persistencia y garantizar concesionario por defecto")
    void testAppContextInitWithStoragePath(@TempDir Path tempDir) throws Exception {
        Path storageFile = tempDir.resolve("dealership.dat");
        assertFalse(Files.exists(storageFile));

        AppContext.init(storageFile);

        assertNotNull(AppContext.getDealership(), "Dealership no debe ser nulo tras init");
        assertNotNull(AppContext.getDealership().getAdministrator(), "Debe contar con el Administrador raíz");
        assertEquals(storageFile, AppContext.getStoragePath());
        assertFalse(AppContext.isAuthenticated(), "No debe haber sesión iniciada al arranque");
        assertNull(AppContext.getCurrentUser());
    }

    @Test
    @DisplayName("Debe gestionar inicio de sesión, roles (Admin/Employee) y cierre de sesión seguro")
    void testAppContextLoginAndLogout(@TempDir Path tempDir) {
        Path storageFile = tempDir.resolve("dealership.dat");
        AppContext.init(storageFile);

        User admin = AppContext.getDealership().getAdministrator();
        AppContext.login(admin);

        assertTrue(AppContext.isAuthenticated(), "Debe indicar que hay una sesión autenticada");
        assertTrue(AppContext.isAdmin(), "El rol activo debe ser Administrador");
        assertFalse(AppContext.isEmployee(), "No debe ser rol Empleado");
        assertSame(admin, AppContext.getCurrentUser());

        AppContext.logout();
        assertFalse(AppContext.isAuthenticated(), "Tras logout no debe haber sesión activa");
        assertNull(AppContext.getCurrentUser());
        assertFalse(AppContext.isAdmin());
        assertFalse(AppContext.isEmployee());
    }

    @Test
    @DisplayName("Debe identificar correctamente el rol Empleado")
    void testAppContextEmployeeRole(@TempDir Path tempDir) throws Exception {
        Path storageFile = tempDir.resolve("dealership.dat");
        AppContext.init(storageFile);

        Employee emp = new Employee("EMP-777", "Diana", "Lopez", "3007778899",
                "diana@omnidrive.com", "diana.lopez", "Diana1234*");
        AppContext.getDealership().registerEmployee(emp);

        AppContext.login(emp);

        assertTrue(AppContext.isAuthenticated());
        assertTrue(AppContext.isEmployee(), "El rol activo debe ser Empleado");
        assertFalse(AppContext.isAdmin(), "No debe ser Administrador");
        assertEquals("EMP-777", AppContext.getCurrentUser().getId());
    }

    @Test
    @DisplayName("Debe persistir el estado del concesionario a disco mediante save()")
    void testAppContextSavePersistence(@TempDir Path tempDir) throws Exception {
        Path storageFile = tempDir.resolve("dealership.dat");
        AppContext.init(storageFile);

        AppContext.getDealership().setName("OmniDrive Sucursal Central Test");
        AppContext.save();

        assertTrue(Files.exists(storageFile), "El archivo en disco debe existir tras save()");
        Dealership loaded = DealershipPersistenceManager.load(storageFile);
        assertEquals("OmniDrive Sucursal Central Test", loaded.getName());
    }

    @Test
    @DisplayName("Debe rechazar ruta de almacenamiento nula")
    void testAppContextRejectsNullStoragePath() {
        assertThrows(IllegalArgumentException.class, () -> AppContext.init(null));
    }
}
