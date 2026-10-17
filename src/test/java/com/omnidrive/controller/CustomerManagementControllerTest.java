package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.model.Customer;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Controlador de Gestión de Clientes (T10 - Spec 006)")
class CustomerManagementControllerTest {

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

    private CustomerManagementController createControllerWithUI() {
        CustomerManagementController controller = new CustomerManagementController();
        controller.setCustomerTable(new TableView<>());
        controller.setColId(new TableColumn<>());
        controller.setColFullName(new TableColumn<>());
        controller.setColPhone(new TableColumn<>());
        controller.setColEmail(new TableColumn<>());

        controller.setTxtSearch(new TextField());
        controller.setTxtId(new TextField());
        controller.setTxtFirstName(new TextField());
        controller.setTxtLastName(new TextField());
        controller.setTxtPhone(new TextField());
        controller.setTxtEmail(new TextField());

        controller.setBtnRegister(new Button());
        controller.setBtnUpdate(new Button());
        controller.setBtnClear(new Button());

        controller.initialize();
        return controller;
    }

    @Test
    @DisplayName("Debe registrar un nuevo cliente exitosamente y actualizar la tabla (RF-06)")
    void testRegisterCustomerSuccess() {
        CustomerManagementController controller = createControllerWithUI();

        controller.getTxtId().setText("1094888999");
        controller.getTxtFirstName().setText("Mariana");
        controller.getTxtLastName().setText("Salazar");
        controller.getTxtPhone().setText("3123456789");
        controller.getTxtEmail().setText("mariana.salazar@empresa.com");

        boolean result = controller.processRegisterCustomer();

        assertTrue(result, "El registro del cliente debe ser exitoso");
        assertEquals(1, AppContext.getDealership().getCustomerCount());
        Customer registered = AppContext.getDealership().findCustomerById("1094888999");
        assertNotNull(registered);
        assertEquals("Mariana Salazar", registered.getFullName());
        assertEquals(1, controller.getCustomerTable().getItems().size());
    }

    @Test
    @DisplayName("Debe rechazar el registro de un cliente con cédula duplicada")
    void testRegisterDuplicateCustomerId() throws Exception {
        Customer existing = new Customer("1094888999", "Mariana", "Salazar", "3123456789", "mariana@test.com");
        AppContext.getDealership().registerCustomer(existing);

        CustomerManagementController controller = createControllerWithUI();

        // Mismo ID
        controller.getTxtId().setText("1094888999");
        controller.getTxtFirstName().setText("Claudia");
        controller.getTxtLastName().setText("Ochoa");
        controller.getTxtPhone().setText("3101112233");
        controller.getTxtEmail().setText("claudia@test.com");

        boolean result = controller.processRegisterCustomer();
        assertFalse(result, "Debe rechazar cliente con cédula duplicada");
        assertEquals(1, AppContext.getDealership().getCustomerCount());
    }

    @Test
    @DisplayName("Debe rechazar el registro si el correo electrónico no cumple formato RFC válido")
    void testRegisterInvalidEmailFormat() {
        CustomerManagementController controller = createControllerWithUI();

        controller.getTxtId().setText("1094777888");
        controller.getTxtFirstName().setText("Esteban");
        controller.getTxtLastName().setText("Quintero");
        controller.getTxtPhone().setText("3159998877");
        controller.getTxtEmail().setText("correo-invalido-sin-arroba");

        boolean result = controller.processRegisterCustomer();
        assertFalse(result, "Debe rechazar correo inválido");
        assertEquals(0, AppContext.getDealership().getCustomerCount());
    }

    @Test
    @DisplayName("Debe actualizar los datos de contacto de un cliente existente")
    void testUpdateCustomerData() throws Exception {
        Customer customer = new Customer("CUST-55", "Lucía", "Herrera", "3004445566", "lucia@test.com");
        AppContext.getDealership().registerCustomer(customer);

        CustomerManagementController controller = createControllerWithUI();
        controller.selectCustomer(customer);

        controller.getTxtFirstName().setText("Lucía Fernanda");
        controller.getTxtPhone().setText("3008889900");
        controller.getTxtEmail().setText("lucia.fernanda@nueva.com");

        boolean updateResult = controller.processUpdateCustomer();
        assertTrue(updateResult, "La actualización del cliente debe ser exitosa");

        Customer updated = AppContext.getDealership().findCustomerById("CUST-55");
        assertEquals("Lucía Fernanda", updated.getFirstName());
        assertEquals("3008889900", updated.getPhone());
        assertEquals("lucia.fernanda@nueva.com", updated.getEmail());
    }

    @Test
    @DisplayName("Debe filtrar clientes en tiempo real por búsqueda de identificación o nombre")
    void testFilterCustomersRealTime() throws Exception {
        AppContext.getDealership().registerCustomer(new Customer("101", "Felipe", "Duque", "3001", "felipe@test.com"));
        AppContext.getDealership().registerCustomer(new Customer("202", "Gloria", "Valencia", "3002", "gloria@test.com"));

        CustomerManagementController controller = createControllerWithUI();
        assertEquals(2, controller.getCustomerTable().getItems().size());

        // Búsqueda por nombre
        controller.getTxtSearch().setText("Gloria");
        controller.applyFilter();
        assertEquals(1, controller.getCustomerTable().getItems().size());
        assertEquals("202", controller.getCustomerTable().getItems().get(0).getId());

        // Búsqueda por cédula
        controller.getTxtSearch().setText("101");
        controller.applyFilter();
        assertEquals(1, controller.getCustomerTable().getItems().size());
        assertEquals("101", controller.getCustomerTable().getItems().get(0).getId());

        // Limpiar búsqueda
        controller.getTxtSearch().setText("");
        controller.applyFilter();
        assertEquals(2, controller.getCustomerTable().getItems().size());
    }
}
