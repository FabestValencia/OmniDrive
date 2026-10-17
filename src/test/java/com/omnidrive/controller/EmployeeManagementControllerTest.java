package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.model.Employee;
import com.omnidrive.model.enums.EmployeeStatus;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Controlador de Gestión de Empleados (T7 - Spec 006)")
class EmployeeManagementControllerTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit JavaFX ya iniciado
        }
    }

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        AppContext.reset();
        AppContext.init(tempDir.resolve("dealership.dat"));
        AppContext.login(AppContext.getDealership().getAdministrator());
    }

    private EmployeeManagementController createControllerWithUI() {
        EmployeeManagementController controller = new EmployeeManagementController();
        controller.setEmployeeTable(new TableView<>());
        controller.setColId(new TableColumn<>());
        controller.setColFullName(new TableColumn<>());
        controller.setColPhone(new TableColumn<>());
        controller.setColEmail(new TableColumn<>());
        controller.setColUsername(new TableColumn<>());
        controller.setColStatus(new TableColumn<>());

        controller.setTxtSearch(new TextField());
        controller.setTxtId(new TextField());
        controller.setTxtFirstName(new TextField());
        controller.setTxtLastName(new TextField());
        controller.setTxtPhone(new TextField());
        controller.setTxtEmail(new TextField());
        controller.setTxtUsername(new TextField());
        controller.setTxtPassword(new PasswordField());

        controller.setBtnRegister(new Button());
        controller.setBtnUpdate(new Button());
        controller.setBtnBlock(new Button());
        controller.setBtnClear(new Button());

        controller.initialize();
        return controller;
    }

    @Test
    @DisplayName("Debe registrar un nuevo empleado exitosamente y actualizar la tabla")
    void testRegisterEmployeeSuccess() {
        EmployeeManagementController controller = createControllerWithUI();

        controller.getTxtId().setText("1094123456");
        controller.getTxtFirstName().setText("Sofía");
        controller.getTxtLastName().setText("Morales");
        controller.getTxtPhone().setText("3101234567");
        controller.getTxtEmail().setText("sofia@omnidrive.com");
        controller.getTxtUsername().setText("smorales");
        controller.getTxtPassword().setText("SofiaPass123*");

        boolean result = controller.processRegisterEmployee();

        assertTrue(result, "El registro del empleado debe ser exitoso");
        assertEquals(1, AppContext.getDealership().getEmployeeCount());
        Employee registered = AppContext.getDealership().findEmployeeById("1094123456");
        assertNotNull(registered);
        assertEquals("Sofía Morales", registered.getFullName());
        assertEquals(EmployeeStatus.ACTIVE, registered.getStatus());
        assertEquals(1, controller.getEmployeeTable().getItems().size());
    }

    @Test
    @DisplayName("Debe rechazar el registro con cédula o nombre de usuario duplicado")
    void testRegisterEmployeeDuplicateRejection() throws Exception {
        Employee existing = new Employee("1094123456", "Sofía", "Morales", "3101234567", "sofia@omnidrive.com", "smorales", "Pass123*");
        AppContext.getDealership().registerEmployee(existing);

        EmployeeManagementController controller = createControllerWithUI();

        // Intento con misma cédula
        controller.getTxtId().setText("1094123456");
        controller.getTxtFirstName().setText("Camila");
        controller.getTxtLastName().setText("Rojas");
        controller.getTxtPhone().setText("3119876543");
        controller.getTxtEmail().setText("camila@omnidrive.com");
        controller.getTxtUsername().setText("crojas");
        controller.getTxtPassword().setText("Pass123*");

        boolean result = controller.processRegisterEmployee();
        assertFalse(result, "Debe rechazar cédula duplicada");
        assertEquals(1, AppContext.getDealership().getEmployeeCount());
    }

    @Test
    @DisplayName("Debe actualizar los datos personales de un colaborador existente")
    void testUpdateEmployeePersonalData() throws Exception {
        Employee emp = new Employee("1094123456", "Carlos", "Pérez", "3001234567", "carlos@omnidrive.com", "cperez", "Pass123*");
        AppContext.getDealership().registerEmployee(emp);

        EmployeeManagementController controller = createControllerWithUI();
        controller.selectEmployee(emp);

        controller.getTxtFirstName().setText("Carlos Alberto");
        controller.getTxtPhone().setText("3009998877");
        controller.getTxtEmail().setText("carlos.nuevo@omnidrive.com");

        boolean updateResult = controller.processUpdateEmployee();
        assertTrue(updateResult, "La actualización debe ser exitosa");

        Employee updated = AppContext.getDealership().findEmployeeById("1094123456");
        assertEquals("Carlos Alberto", updated.getFirstName());
        assertEquals("3009998877", updated.getPhone());
        assertEquals("carlos.nuevo@omnidrive.com", updated.getEmail());
    }

    @Test
    @DisplayName("Debe bloquear permanentemente la cuenta de un empleado (RF-05, RF-11)")
    void testBlockEmployeePermanently() throws Exception {
        Employee emp = new Employee("1094123456", "David", "Vargas", "3151234567", "david@omnidrive.com", "dvargas", "Pass123*");
        AppContext.getDealership().registerEmployee(emp);

        EmployeeManagementController controller = createControllerWithUI();
        controller.selectEmployee(emp);

        boolean blockResult = controller.processBlockEmployee(emp.getId());
        assertTrue(blockResult, "El bloqueo debe ser exitoso");

        Employee blocked = AppContext.getDealership().findEmployeeById("1094123456");
        assertTrue(blocked.isBlocked(), "El empleado debe quedar bloqueado permanentemente");
        assertEquals(EmployeeStatus.BLOCKED, blocked.getStatus());
    }

    @Test
    @DisplayName("Debe filtrar colaboradores por búsqueda en tiempo real")
    void testFilterEmployeesBySearch() throws Exception {
        AppContext.getDealership().registerEmployee(new Employee("101", "Andrés", "Castro", "3001", "andres@test.com", "acastro", "Pass123*"));
        AppContext.getDealership().registerEmployee(new Employee("102", "Beatriz", "Pinzón", "3002", "betty@test.com", "bpinzon", "Pass123*"));

        EmployeeManagementController controller = createControllerWithUI();
        assertEquals(2, controller.getEmployeeTable().getItems().size());

        controller.getTxtSearch().setText("Castro");
        controller.applyFilter();
        assertEquals(1, controller.getEmployeeTable().getItems().size());
        assertEquals("101", controller.getEmployeeTable().getItems().get(0).getId());

        controller.getTxtSearch().setText("Pinzón");
        controller.applyFilter();
        assertEquals(1, controller.getEmployeeTable().getItems().size());
        assertEquals("102", controller.getEmployeeTable().getItems().get(0).getId());

        controller.getTxtSearch().setText("");
        controller.applyFilter();
        assertEquals(2, controller.getEmployeeTable().getItems().size());
    }

    @Test
    @DisplayName("Debe aplicar clase CSS text-field-error ante campos inválidos en empleado")
    void testValidationMarksErrorStyleInEmployee() {
        EmployeeManagementController controller = createControllerWithUI();
        controller.getTxtId().setText("12345");
        controller.getTxtFirstName().setText("Mario");
        controller.getTxtLastName().setText("Calderón");
        controller.getTxtPhone().setText("abc"); // Teléfono inválido
        controller.getTxtEmail().setText("mario@valido.com");
        controller.getTxtUsername().setText("mcalderon");
        controller.getTxtPassword().setText("Secret123*");

        boolean result = controller.processRegisterEmployee();
        assertFalse(result);
        assertTrue(controller.getTxtPhone().getStyleClass().contains("text-field-error"));
        assertFalse(controller.getTxtId().getStyleClass().contains("text-field-error"));

        // Corregir y reenviar
        controller.getTxtPhone().setText("3005556677");
        boolean ok = controller.processRegisterEmployee();
        assertTrue(ok);
        assertFalse(controller.getTxtPhone().getStyleClass().contains("text-field-error"));
    }
}
