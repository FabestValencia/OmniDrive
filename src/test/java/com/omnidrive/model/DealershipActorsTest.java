package com.omnidrive.model;

import com.omnidrive.exception.DuplicateUserException;
import com.omnidrive.exception.InvalidUserDataException;
import com.omnidrive.model.enums.EmployeeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Gestión de Actores en Dealership (T5)")
class DealershipActorsTest {

    private Dealership dealership;

    @BeforeEach
    void setUp() {
        dealership = new Dealership("OmniDrive Central");
    }

    @Test
    @DisplayName("Debe inicializar un Administrador raíz por defecto (RF-20)")
    void testRootAdministratorInitialized() {
        Administrator admin = dealership.getAdministrator();
        assertNotNull(admin);
        assertEquals("admin", admin.getUsername());
        assertTrue(admin.verifyPassword("Admin123*"));
    }

    @Test
    @DisplayName("Debe registrar un Cliente exitosamente validando su cédula única (RF-12)")
    void testRegisterCustomerSuccessfully() throws Exception {
        Customer customer = new Customer("1094888999", "Carlos", "Perez", "3101234567", "carlos@example.com");
        dealership.registerCustomer(customer);

        assertEquals(1, dealership.getCustomerCount());
        assertEquals(customer, dealership.findCustomerById("1094888999"));
    }

    @Test
    @DisplayName("Debe rechazar registrar cliente con cédula duplicada (RF-17)")
    void testRejectDuplicateCustomerId() throws Exception {
        Customer c1 = new Customer("1094888999", "Carlos", "Perez", "3101234567", "carlos@example.com");
        Customer c2 = new Customer("1094888999", "Ana", "Gomez", "3159876543", "ana@example.com");

        dealership.registerCustomer(c1);

        DuplicateUserException ex = assertThrows(DuplicateUserException.class, () -> dealership.registerCustomer(c2));
        assertTrue(ex.getMessage().contains("1094888999"));
    }

    @Test
    @DisplayName("Debe registrar un Empleado exitosamente con estado inicial activo (RF-05, RF-09)")
    void testRegisterEmployeeSuccessfully() throws Exception {
        Employee emp = new Employee("1094111222", "Laura", "Gomez", "3123456789", "laura@omnidrive.com", "laura.ventas", "Pass123*");
        dealership.registerEmployee(emp);

        assertEquals(1, dealership.getEmployeeCount());
        assertEquals(emp, dealership.findEmployeeById("1094111222"));
        assertEquals(emp, dealership.findEmployeeByUsername("laura.ventas"));
        assertEquals(EmployeeStatus.ACTIVE, emp.getStatus());
    }

    @Test
    @DisplayName("Debe rechazar empleado con cédula o nombre de usuario duplicado (RF-17)")
    void testRejectDuplicateEmployee() throws Exception {
        Employee emp1 = new Employee("1094111222", "Laura", "Gomez", "3123456789", "laura@omnidrive.com", "laura.ventas", "Pass123*");
        dealership.registerEmployee(emp1);

        // Mismo id, distinto username
        Employee empDupId = new Employee("1094111222", "Otra", "Persona", "3001112233", "otra@omnidrive.com", "otra.persona", "Pass123*");
        assertThrows(DuplicateUserException.class, () -> dealership.registerEmployee(empDupId));

        // Distinto id, mismo username
        Employee empDupUser = new Employee("1094999888", "Mismo", "Usuario", "3001112233", "mismo@omnidrive.com", "laura.ventas", "Pass123*");
        assertThrows(DuplicateUserException.class, () -> dealership.registerEmployee(empDupUser));

        // Mismo username que el Administrador raíz
        Employee empDupAdmin = new Employee("1094777666", "Admin", "Falso", "3001112233", "falso@omnidrive.com", "admin", "Pass123*");
        assertThrows(DuplicateUserException.class, () -> dealership.registerEmployee(empDupAdmin));
    }

    @Test
    @DisplayName("Debe permitir al Administrador actualizar los datos personales de un empleado (RF-10)")
    void testUpdateEmployeeData() throws Exception {
        Employee emp = new Employee("1094111222", "Laura", "Gomez", "3123456789", "laura@omnidrive.com", "laura.ventas", "Pass123*");
        dealership.registerEmployee(emp);

        dealership.updateEmployee("1094111222", "Laura Sofia", "Gomez Diaz", "3100000000", "laura.sofia@omnidrive.com");

        Employee updated = dealership.findEmployeeById("1094111222");
        assertEquals("Laura Sofia", updated.getFirstName());
        assertEquals("Gomez Diaz", updated.getLastName());
        assertEquals("3100000000", updated.getPhone());
        assertEquals("laura.sofia@omnidrive.com", updated.getEmail());
    }

    @Test
    @DisplayName("Debe permitir al Administrador bloquear permanentemente a un empleado (RF-11)")
    void testBlockEmployeePermanently() throws Exception {
        Employee emp = new Employee("1094111222", "Laura", "Gomez", "3123456789", "laura@omnidrive.com", "laura.ventas", "Pass123*");
        dealership.registerEmployee(emp);
        assertFalse(emp.isBlocked());

        dealership.blockEmployee("1094111222");

        Employee blocked = dealership.findEmployeeById("1094111222");
        assertTrue(blocked.isBlocked());
        assertEquals(EmployeeStatus.BLOCKED, blocked.getStatus());
    }

    @Test
    @DisplayName("Debe rechazar actualizar o bloquear a un empleado inexistente")
    void testRejectOperationsOnNonExistentEmployee() {
        assertThrows(InvalidUserDataException.class, () ->
                dealership.updateEmployee("NO_EXISTE", "Nombre", "Apellido", "3100000000", "email@test.com")
        );
        assertThrows(InvalidUserDataException.class, () ->
                dealership.blockEmployee("NO_EXISTE")
        );
    }
}
