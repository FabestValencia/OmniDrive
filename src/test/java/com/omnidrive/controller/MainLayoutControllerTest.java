package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.model.Employee;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Controlador de MainLayout / Shell (T5 - Spec 006)")
class MainLayoutControllerTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit de JavaFX ya iniciado
        }
    }

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        AppContext.reset();
        AppContext.init(tempDir.resolve("dealership.dat"));
    }

    private MainLayoutController createControllerWithUI() {
        MainLayoutController controller = new MainLayoutController();
        controller.setBtnDashboard(new Button());
        controller.setBtnVehicles(new Button());
        controller.setBtnCustomers(new Button());
        controller.setBtnTransactions(new Button());
        controller.setBtnEmployees(new Button());
        controller.setBtnReports(new Button());
        controller.setBtnLogout(new Button());

        controller.setLblUserName(new Label());
        controller.setLblUserRole(new Label());
        controller.setLblSectionTitle(new Label());
        controller.setContentArea(new StackPane());

        return controller;
    }

    @Test
    @DisplayName("Debe configurar barra lateral completa para Administrador (RF-05, Escenario 1)")
    void testSidebarVisibilityForAdministrator() {
        // Autenticar como Administrador
        AppContext.login(AppContext.getDealership().getAdministrator());

        MainLayoutController controller = createControllerWithUI();
        controller.refreshUserSession();

        // RF-05: Habilita Empleados y Reportes. Oculta transacciones directas para Admin.
        assertTrue(controller.getBtnEmployees().isVisible(), "Admin debe ver botón de Empleados");
        assertTrue(controller.getBtnEmployees().isManaged(), "Admin debe tener gestionado botón de Empleados");

        assertTrue(controller.getBtnReports().isVisible(), "Admin debe ver botón de Reportes");
        assertTrue(controller.getBtnReports().isManaged(), "Admin debe tener gestionado botón de Reportes");

        assertFalse(controller.getBtnTransactions().isVisible(), "Admin no realiza transacciones operativas");
        assertFalse(controller.getBtnTransactions().isManaged());

        assertTrue(controller.getBtnVehicles().isVisible(), "Admin debe ver Inventario");
        assertTrue(controller.getBtnCustomers().isVisible(), "Admin debe ver Clientes");

        assertEquals("Administrador", controller.getLblUserRole().getText());
        assertEquals("Admin OmniDrive", controller.getLblUserName().getText());
    }

    @Test
    @DisplayName("Debe adaptar barra lateral para Empleado ocultando Empleados y Reportes (RF-06)")
    void testSidebarVisibilityForEmployee() throws Exception {
        // Crear y autenticar un Empleado
        Employee emp = new Employee("EMP-101", "Carlos", "Gómez", "3001234567", "carlos@omnidrive.com", "cgomez", "EmpPass123*");
        AppContext.getDealership().registerEmployee(emp);
        AppContext.login(emp);

        MainLayoutController controller = createControllerWithUI();
        controller.refreshUserSession();

        // RF-06: Oculta Empleados y Reportes, habilita Vehículos, Clientes y Transacciones
        assertFalse(controller.getBtnEmployees().isVisible(), "Empleado no debe ver módulo de Empleados");
        assertFalse(controller.getBtnEmployees().isManaged());

        assertFalse(controller.getBtnReports().isVisible(), "Empleado no debe ver módulo de Reportes");
        assertFalse(controller.getBtnReports().isManaged());

        assertTrue(controller.getBtnTransactions().isVisible(), "Empleado debe ver Transacciones");
        assertTrue(controller.getBtnTransactions().isManaged());

        assertTrue(controller.getBtnVehicles().isVisible(), "Empleado debe ver Inventario");
        assertTrue(controller.getBtnCustomers().isVisible(), "Empleado debe ver Clientes");

        assertEquals("Empleado", controller.getLblUserRole().getText());
        assertEquals("Carlos Gómez", controller.getLblUserName().getText());
    }

    @Test
    @DisplayName("Debe actualizar título de sección y clase activa al navegar")
    void testNavigationUpdatesSectionAndActiveStyle() {
        AppContext.login(AppContext.getDealership().getAdministrator());
        MainLayoutController controller = createControllerWithUI();
        controller.refreshUserSession();

        // Navegar a Vehículos
        controller.showVehicles();
        assertEquals("Inventario de Vehículos", controller.getLblSectionTitle().getText());
        assertTrue(controller.getBtnVehicles().getStyleClass().contains("active"));
        assertFalse(controller.getBtnDashboard().getStyleClass().contains("active"));

        // Navegar a Clientes
        controller.showCustomers();
        assertEquals("Gestión de Clientes", controller.getLblSectionTitle().getText());
        assertTrue(controller.getBtnCustomers().getStyleClass().contains("active"));
        assertFalse(controller.getBtnVehicles().getStyleClass().contains("active"));
    }

    @Test
    @DisplayName("Cerrar sesión debe limpiar AppContext y disparar callback de logout")
    void testHandleLogout() {
        AppContext.login(AppContext.getDealership().getAdministrator());
        assertTrue(AppContext.isAuthenticated());

        MainLayoutController controller = createControllerWithUI();
        AtomicBoolean logoutFired = new AtomicBoolean(false);
        controller.setOnLogoutRequested(() -> logoutFired.set(true));

        controller.handleLogout();

        assertFalse(AppContext.isAuthenticated(), "La sesión debe haberse cerrado");
        assertNull(AppContext.getCurrentUser(), "El usuario actual debe ser null");
        assertTrue(logoutFired.get(), "El callback de logout debió ser ejecutado");
    }

    @Test
    @DisplayName("Debe alojar y reemplazar vistas secundarias en el área de contenido principal")
    void testSetContent() {
        MainLayoutController controller = createControllerWithUI();
        Label dummyView = new Label("Contenido de prueba");

        controller.setContent(dummyView);

        assertEquals(1, controller.getContentArea().getChildren().size());
        assertSame(dummyView, controller.getContentArea().getChildren().get(0));
    }
}
