package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.model.*;
import com.omnidrive.model.enums.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Controlador de Dashboard (T6 - Spec 006)")
class DashboardControllerTest {

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

    private DashboardController createControllerWithUI() {
        DashboardController controller = new DashboardController();
        controller.setLblWelcome(new Label());
        controller.setLblUserGreeting(new Label());
        controller.setLblAvailableVehicles(new Label());
        controller.setLblTotalCustomers(new Label());
        controller.setLblTotalTransactions(new Label());
        controller.setLblActiveEmployees(new Label());
        controller.setLblRentedVehicles(new Label());
        controller.setCardEmployees(new VBox());
        return controller;
    }

    @Test
    @DisplayName("Debe mostrar métricas iniciales coherentes en concesionario recién inicializado")
    void testInitialMetrics() {
        AppContext.login(AppContext.getDealership().getAdministrator());
        DashboardController controller = createControllerWithUI();
        controller.refreshMetrics();

        assertEquals("0", controller.getLblAvailableVehicles().getText());
        assertEquals("0", controller.getLblTotalCustomers().getText());
        assertEquals("0", controller.getLblTotalTransactions().getText());
        assertEquals("0", controller.getLblActiveEmployees().getText());
        assertEquals("0", controller.getLblRentedVehicles().getText());
        assertTrue(controller.getLblWelcome().getText().contains("Bienvenido"));
    }

    @Test
    @DisplayName("Debe reflejar incrementos de inventario, clientes y empleados en los KPIs")
    void testMetricsWithPopulatedDealership() throws Exception {
        Dealership dealership = AppContext.getDealership();
        AppContext.login(dealership.getAdministrator());

        // 1. Agregar clientes
        Customer c1 = new Customer("CUST-01", "Ana", "López", "3001112233", "ana@test.com");
        Customer c2 = new Customer("CUST-02", "Juan", "Pérez", "3002223344", "juan@test.com");
        dealership.registerCustomer(c1);
        dealership.registerCustomer(c2);

        // 2. Agregar empleados (1 activo, 1 bloqueado)
        Employee e1 = new Employee("EMP-01", "Mario", "Díaz", "3003334455", "mario@test.com", "mdiaz", "Pass123*");
        Employee e2 = new Employee("EMP-02", "Laura", "Ríos", "3004445566", "laura@test.com", "lrios", "Pass123*");
        e2.block();
        dealership.registerEmployee(e1);
        dealership.registerEmployee(e2);

        // 3. Agregar vehículos
        Sedan sedan = new Sedan("ABC-123", "Toyota", 2024, 6, 200.0, 1800.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 80_000_000.0, "toyota.jpg",
                true, null, null,
                5, 4, 6, true, true, true,
                450.0, true, true, true, true);
        dealership.registerVehicle(sedan);

        Van van = new Van("VAN-999", "Mercedes", 2023, 6, 170.0, 2200.0,
                FuelType.DIESEL, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_RENT, 150_000.0, "van.jpg",
                true, null, null,
                12, 4, 4, true, true,
                true, 800.0);
        dealership.registerVehicle(van);

        // 4. Realizar una transacción de alquiler
        dealership.rentVehicle(e1, c1, "VAN-999",
                LocalDate.now(), LocalDate.now().plusDays(5), 5, Instant.now());

        DashboardController controller = createControllerWithUI();
        controller.refreshMetrics();

        // Verificación de KPIs
        assertEquals("1", controller.getLblAvailableVehicles().getText(), "Solo el sedán queda disponible (la van pasó a alquilada)");
        assertEquals("2", controller.getLblTotalCustomers().getText());
        assertEquals("1", controller.getLblTotalTransactions().getText());
        assertEquals("1", controller.getLblActiveEmployees().getText(), "Solo 1 de los 2 empleados está activo");
        assertEquals("1", controller.getLblRentedVehicles().getText(), "La van está en arriendo");
    }

    @Test
    @DisplayName("Debe adaptar saludo y visibilidad de tarjetas según el rol del usuario")
    void testDashboardRoleAdaptation() throws Exception {
        Dealership dealership = AppContext.getDealership();
        Employee emp = new Employee("EMP-01", "Pedro", "Navaja", "3005556677", "pedro@test.com", "pnavaja", "Pass123*");
        dealership.registerEmployee(emp);

        // Caso 1: Administrador
        AppContext.login(dealership.getAdministrator());
        DashboardController adminController = createControllerWithUI();
        adminController.refreshMetrics();
        assertTrue(adminController.getCardEmployees().isVisible(), "Admin debe ver tarjeta de colaboradores");
        assertTrue(adminController.getLblUserGreeting().getText().contains("Admin OmniDrive"));

        // Caso 2: Empleado
        AppContext.login(emp);
        DashboardController empController = createControllerWithUI();
        empController.refreshMetrics();
        assertFalse(empController.getCardEmployees().isVisible(), "Empleado regular no requiere métrica de empleados");
        assertTrue(empController.getLblUserGreeting().getText().contains("Pedro Navaja"));
    }
}
