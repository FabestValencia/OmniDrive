package com.omnidrive.util;

import com.omnidrive.exception.BlockedUserException;
import com.omnidrive.exception.PersistenceException;
import com.omnidrive.model.*;
import com.omnidrive.model.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Escenarios de Aceptación Oficiales - Persistencia (Spec 005)")
class PersistenceAcceptanceScenariosTest {

    @Test
    @DisplayName("Escenario 1: Inicialización automática en primer arranque (archivo ausente)")
    void testScenario1_ColdStartInitializationWhenFileDoesNotExist(@TempDir Path tempDir) throws Exception {
        // Dado que no existe previamente ningún archivo de datos en la ruta configurada
        Path absentPath = tempDir.resolve("virgin_dealership_data.dat");
        assertFalse(Files.exists(absentPath));

        // Cuando el sistema ejecuta la carga inicial de datos
        Dealership dealership = DealershipPersistenceManager.loadOrCreateDefault(absentPath);

        // Entonces el sistema inicializa una nueva instancia de concesionario, garantiza la presencia del
        // Administrador raíz (ADMIN-001, admin) y permite iniciar sesión de forma inmediata
        assertNotNull(dealership);
        assertNotNull(dealership.getAdministrator());
        assertEquals("ADMIN-001", dealership.getAdministrator().getId());
        assertEquals("admin", dealership.getAdministrator().getUsername());

        User authenticated = dealership.authenticate("admin", "Admin123*");
        assertNotNull(authenticated);
        assertEquals("ADMIN-001", authenticated.getId());
    }

    @Test
    @DisplayName("Escenario 2: Ciclo completo de guardado y restauración exitosa")
    void testScenario2_FullSaveAndRestoreCycle(@TempDir Path tempDir) throws Exception {
        // Dado un concesionario con 3 vehículos registrados, 2 empleados (uno activo y uno bloqueado),
        // 1 cliente y 2 transacciones de compra/venta
        Dealership dealership = new Dealership("OmniDrive Central");

        Employee activeEmp = new Employee("EMP-001", "Sofia", "Rojas", "3001234567",
                "sofia@omnidrive.com", "sofia.rojas", "Sofia2026*");
        Employee blockedEmp = new Employee("EMP-002", "Lucas", "Mora", "3009876543",
                "lucas@omnidrive.com", "lucas.mora", "Lucas2026*");
        dealership.registerEmployee(activeEmp);
        dealership.registerEmployee(blockedEmp);
        dealership.blockEmployee(blockedEmp.getId());

        Customer customer = new Customer("CUST-001", "Andres", "Vargas", "3112223344",
                "andres.vargas@example.com");
        dealership.registerCustomer(customer);

        Sedan sedan1 = new Sedan("V01-AAA", "Toyota", 2024, 6, 200.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 70000000.0, null, true, null, null,
                5, 4, 6, true, true, true, 450.0, true, true, true, true);

        Motorcycle moto = new Motorcycle("V02-BBB", "Yamaha", 2023, 6, 170.0, 300.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 22000000.0, null, true, null, null);

        dealership.registerVehicle(sedan1);
        dealership.registerVehicle(moto);

        // Transacción 1: Compra de un vehículo usado con revisión aprobada (3er vehículo)
        Sedan usedToBuy = new Sedan("V03-CCC", "Mazda", 2020, 6, 190.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.USED,
                VehicleStatus.AVAILABLE_FOR_SALE, 40000000.0, null, true, null, null,
                5, 4, 6, true, true, true, 420.0, true, true, true, true);
        PurchaseTransaction purchase = dealership.buyVehicle(activeEmp, customer, usedToBuy,
                38000000.0, VehicleStatus.AVAILABLE_FOR_SALE, Instant.now());

        // Transacción 2: Venta de sedan1
        SaleTransaction sale = dealership.sellVehicle(activeEmp, customer, "V01-AAA",
                70000000.0, Instant.now());

        assertEquals(3, dealership.getVehicleCount());
        assertEquals(2, dealership.getEmployeeCount());
        assertEquals(1, dealership.getCustomerCount());
        assertEquals(2, dealership.getAllTransactions().size());

        // Cuando se ejecuta la operación de guardado en archivo local y posteriormente se carga en una nueva instancia
        Path targetFile = tempDir.resolve("acceptance_scenario2.dat");
        DealershipPersistenceManager.save(dealership, targetFile);

        Dealership loaded = DealershipPersistenceManager.load(targetFile);

        // Entonces la instancia restaurada conserva exactamente:
        // - Los 3 vehículos con sus estados correspondientes
        assertNotNull(loaded);
        assertEquals(3, loaded.getVehicleCount());
        assertEquals(VehicleStatus.SOLD, loaded.findVehicleByPlate("V01-AAA").getStatus());
        assertEquals(VehicleStatus.AVAILABLE_FOR_SALE, loaded.findVehicleByPlate("V02-BBB").getStatus());
        assertEquals(VehicleStatus.AVAILABLE_FOR_SALE, loaded.findVehicleByPlate("V03-CCC").getStatus());

        // - Los 2 empleados manteniendo su estado (ACTIVE y BLOCKED)
        assertEquals(2, loaded.getEmployeeCount());
        Employee loadedActive = loaded.findEmployeeById("EMP-001");
        Employee loadedBlocked = loaded.findEmployeeById("EMP-002");
        assertNotNull(loadedActive);
        assertNotNull(loadedBlocked);
        assertEquals(EmployeeStatus.ACTIVE, loadedActive.getStatus());
        assertEquals(EmployeeStatus.BLOCKED, loadedBlocked.getStatus());
        assertNotNull(loaded.authenticate("sofia.rojas", "Sofia2026*"));
        assertThrows(BlockedUserException.class, () -> loaded.authenticate("lucas.mora", "Lucas2026*"));

        // - El cliente registrado
        assertEquals(1, loaded.getCustomerCount());
        Customer loadedCustomer = loaded.findCustomerById("CUST-001");
        assertNotNull(loadedCustomer);
        assertEquals("Andres", loadedCustomer.getFirstName());
        assertEquals("andres.vargas@example.com", loadedCustomer.getEmail());

        // - Las 2 transacciones con sus montos, fechas y referencias intactas
        assertEquals(2, loaded.getAllTransactions().size());
        Transaction loadedPurchase = loaded.findTransactionById(purchase.getId());
        Transaction loadedSale = loaded.findTransactionById(sale.getId());
        assertNotNull(loadedPurchase);
        assertNotNull(loadedSale);
        assertEquals(38000000.0, loadedPurchase.getTotalAmount());
        assertEquals(70000000.0, loadedSale.getTotalAmount());
        assertEquals("EMP-001", loadedPurchase.getEmployee().getId());
        assertEquals("CUST-001", loadedPurchase.getCustomer().getId());
        assertEquals("V03-CCC", loadedPurchase.getVehicle().getPlate());
    }

    @Test
    @DisplayName("Escenario 3: Rechazo ante archivo corrupto o ilegible")
    void testScenario3_RejectCorruptedFile(@TempDir Path tempDir) throws IOException {
        // Dado un archivo en la ruta de persistencia con contenido dañado o bytes aleatorios no serializables
        Path corruptedFile = tempDir.resolve("corrupted_dealership.bin");
        Files.write(corruptedFile, new byte[]{(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF, 0x01, 0x02});

        // Cuando el sistema intenta cargar el concesionario desde ese archivo
        // Entonces el sistema detiene la carga y arroja una excepción de persistencia indicando que el archivo está corrupto
        PersistenceException ex = assertThrows(PersistenceException.class, () ->
                DealershipPersistenceManager.load(corruptedFile)
        );

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("corrupto") ||
                   ex.getMessage().toLowerCase().contains("error") ||
                   ex.getMessage().toLowerCase().contains("deserializar"));
    }

    @Test
    @DisplayName("Escenario 4: Rechazo ante ruta de archivo inaccesible")
    void testScenario4_RejectInaccessiblePath(@TempDir Path tempDir) throws IOException {
        // Dado una ruta de archivo inválida o un directorio sin permisos de escritura
        Dealership dealership = new Dealership("OmniDrive Test");
        Path existingDirectoryAsFile = tempDir.resolve("directory_conflict");
        Files.createDirectory(existingDirectoryAsFile);

        // Cuando se intenta guardar el estado del concesionario en esa ruta
        // Entonces el sistema arroja una excepción de persistencia informando el error de acceso al archivo
        PersistenceException ex = assertThrows(PersistenceException.class, () ->
                DealershipPersistenceManager.save(dealership, existingDirectoryAsFile)
        );

        assertNotNull(ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    @DisplayName("Escenario 5: Rechazo de guardado de concesionario nulo")
    void testScenario5_RejectNullDealershipInstance(@TempDir Path tempDir) {
        // Dado una referencia nula de concesionario
        Path targetFile = tempDir.resolve("valid_path.dat");

        // Cuando se invoca el método de guardado suministrando null
        // Entonces el sistema rechaza la operación informando que la instancia a persistir no puede ser nula
        PersistenceException ex = assertThrows(PersistenceException.class, () ->
                DealershipPersistenceManager.save(null, targetFile)
        );

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("nulo") ||
                   ex.getMessage().toLowerCase().contains("null"));
    }
}
