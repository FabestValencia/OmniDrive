package com.omnidrive.util;

import com.omnidrive.exception.BlockedUserException;
import com.omnidrive.model.*;
import com.omnidrive.model.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Integración - Ciclo Completo de Persistencia del Modelo (T5)")
class DealershipFullPersistenceIntegrationTest {

    @Test
    @DisplayName("Debe persistir y restaurar con 100% de fidelidad el modelo integral (8 vehículos, actores, transacciones y reportes)")
    void testFullPersistenceLifeCycle(@TempDir Path tempDir) throws Exception {
        // 1. Instanciación del concesionario
        Dealership dealership = new Dealership("OmniDrive MegaStore");

        // 2. Registro de Actores
        Employee activeEmp = new Employee("EMP-010", "Elena", "Rios", "3001112233",
                "elena@omnidrive.com", "elena.rios", "Elena2026*");
        Employee blockedEmp = new Employee("EMP-020", "Marcos", "Paz", "3004445566",
                "marcos@omnidrive.com", "marcos.paz", "Marcos2026*");
        dealership.registerEmployee(activeEmp);
        dealership.registerEmployee(blockedEmp);
        dealership.blockEmployee(blockedEmp.getId());

        Customer cust1 = new Customer("CUST-100", "Camila", "Duarte", "3109998877", "camila@example.com");
        Customer cust2 = new Customer("CUST-200", "Diego", "Castro", "3118887766", "diego@example.com");
        dealership.registerCustomer(cust1);
        dealership.registerCustomer(cust2);

        // 3. Registro de los 8 tipos de vehículos (con variantes de combustible)
        Motorcycle moto = new Motorcycle("MOT-01", "Yamaha", 2023, 6, 180.0, 321.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 28000000.0, null, true, null, null);

        HybridDetails hybridSedan = new HybridDetails(true, false);
        Sedan sedan = new Sedan("SED-01", "Honda", 2024, 6, 210.0, 1500.0,
                FuelType.HYBRID, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 95000000.0, null, true, null, hybridSedan,
                5, 4, 6, true, true, true, 450.0, true, true, true, true);

        ElectricDetails electricSports = new ElectricDetails(480.0, 2.5);
        SportsCar sportsCar = new SportsCar("DEP-01", "Porsche", 2024, 7, 305.0, 0.0,
                FuelType.ELECTRIC, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 350000000.0, null, true, electricSports, null,
                2, 2, 6, 650, 2.7);

        Suv suv = new Suv("SUV-01", "Ford", 2023, 8, 220.0, 2300.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_RENT, 160000000.0, null, true, null, null,
                7, 5, 8, true, true, true, 600.0, true, true, true, true, true);

        PickUp pickUp = new PickUp("PCK-01", "Toyota", 2023, 6, 180.0, 2800.0,
                FuelType.DIESEL, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 175000000.0, null, true, null, null,
                5, 4, 6, true, true, true, true, 1100.0);

        Van van = new Van("VAN-01", "Mercedes-Benz", 2024, 6, 170.0, 2200.0,
                FuelType.DIESEL, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 210000000.0, null, true, null, null,
                12, 4, 4, true, true, true, 900.0);

        Bus bus = new Bus("BUS-01", "Scania", 2023, 8, 120.0, 9000.0,
                FuelType.DIESEL, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 600000000.0, null, true, null, null,
                45, 2, 2, true, true, true, 2500.0, 3, 4);

        Truck truck = new Truck("TRK-01", "Volvo", 2024, 12, 110.0, 12800.0,
                FuelType.DIESEL, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 480000000.0, null, true, null, null,
                28.0, true, true, true, 4, TruckType.BOX_TRUCK);

        dealership.registerVehicle(moto);
        dealership.registerVehicle(sedan);
        dealership.registerVehicle(sportsCar);
        dealership.registerVehicle(suv);
        dealership.registerVehicle(pickUp);
        dealership.registerVehicle(van);
        dealership.registerVehicle(bus);
        dealership.registerVehicle(truck);

        // 4. Transacciones comerciales
        // Compra de un vehículo usado con revisión técnica aprobada
        Sedan usedToBuy = new Sedan("USE-99", "Mazda", 2021, 6, 190.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.USED,
                VehicleStatus.AVAILABLE_FOR_SALE, 45000000.0, null, true, null, null,
                5, 4, 6, true, true, true, 420.0, true, true, true, true);
        PurchaseTransaction buyTxn = dealership.buyVehicle(activeEmp, cust1, usedToBuy, 40000000.0,
                VehicleStatus.AVAILABLE_FOR_SALE, Instant.now());
        assertNotNull(buyTxn);

        // Venta del Sedán SED-01
        SaleTransaction saleTxn = dealership.sellVehicle(activeEmp, cust1, "SED-01", 95000000.0, Instant.now());
        assertNotNull(saleTxn);
        assertEquals(VehicleStatus.SOLD, dealership.findVehicleByPlate("SED-01").getStatus());

        // Alquiler y devolución de la Camioneta SUV-01
        RentalTransaction rentTxn = dealership.rentVehicle(activeEmp, cust2, "SUV-01",
                LocalDate.now(), LocalDate.now().plusDays(5), 5, Instant.now());
        assertNotNull(rentTxn);
        assertEquals(VehicleStatus.RENTED, dealership.findVehicleByPlate("SUV-01").getStatus());

        Vehicle returnedVehicle = dealership.returnRentedVehicle("SUV-01");
        assertEquals(VehicleStatus.AVAILABLE_FOR_RENT, returnedVehicle.getStatus());

        // Token de recuperación de contraseña
        String recoveryCode = dealership.requestPasswordRecovery("elena.rios", Instant.now());
        assertNotNull(recoveryCode);

        // 5. Persistir en archivo local
        Path backupFile = tempDir.resolve("dealership_backup.dat");
        DealershipPersistenceManager.save(dealership, backupFile);

        // 6. Restaurar en una instancia totalmente desacoplada
        Dealership loaded = DealershipPersistenceManager.load(backupFile);

        // 7. Aserciones exhaustivas de fidelidad
        assertNotNull(loaded);
        assertEquals("OmniDrive MegaStore", loaded.getName());
        assertEquals(9, loaded.getVehicleCount(), "Debe contener los 8 originales + 1 comprado");
        assertEquals(2, loaded.getCustomerCount());
        assertEquals(2, loaded.getEmployeeCount());

        // Verificación de Vehículos específicos y sus atributos serializados
        Vehicle restoredSedan = loaded.findVehicleByPlate("SED-01");
        assertNotNull(restoredSedan);
        assertEquals(VehicleStatus.SOLD, restoredSedan.getStatus());
        assertTrue(restoredSedan instanceof Sedan);
        Sedan castSedan = (Sedan) restoredSedan;
        assertNotNull(castSedan.getHybridDetails());
        assertTrue(castSedan.getHybridDetails().isPlugIn());

        Vehicle restoredSports = loaded.findVehicleByPlate("DEP-01");
        assertNotNull(restoredSports);
        assertEquals(FuelType.ELECTRIC, restoredSports.getFuelType());
        assertNotNull(restoredSports.getElectricDetails());
        assertEquals(480.0, restoredSports.getElectricDetails().getAutonomyKm());
        assertEquals(2.5, restoredSports.getElectricDetails().getAverageChargingHours());

        Vehicle restoredSuv = loaded.findVehicleByPlate("SUV-01");
        assertNotNull(restoredSuv);
        assertEquals(VehicleStatus.AVAILABLE_FOR_RENT, restoredSuv.getStatus());
        assertTrue(((Suv) restoredSuv).isFourByFour());

        Vehicle restoredTruck = loaded.findVehicleByPlate("TRK-01");
        assertNotNull(restoredTruck);
        Truck castTruck = (Truck) restoredTruck;
        assertEquals(28.0, castTruck.getLoadCapacityTons());
        assertEquals(4, castTruck.getAxleCount());
        assertEquals(TruckType.BOX_TRUCK, castTruck.getTruckType());

        Vehicle restoredBus = loaded.findVehicleByPlate("BUS-01");
        assertNotNull(restoredBus);
        Bus castBus = (Bus) restoredBus;
        assertEquals(45, castBus.getPassengerCount());
        assertEquals(3, castBus.getAxleCount());
        assertEquals(4, castBus.getEmergencyExitCount());

        // Verificación de Actores y Permisos
        assertNotNull(loaded.authenticate("admin", "Admin123*"));
        assertNotNull(loaded.authenticate("elena.rios", "Elena2026*"));
        assertThrows(BlockedUserException.class, () -> loaded.authenticate("marcos.paz", "Marcos2026*"));

        // Verificación de Transacciones y Trazabilidad
        assertEquals(3, loaded.getAllTransactions().size());
        Transaction t1 = loaded.findTransactionById(buyTxn.getId());
        Transaction t2 = loaded.findTransactionById(saleTxn.getId());
        Transaction t3 = loaded.findTransactionById(rentTxn.getId());
        assertNotNull(t1);
        assertNotNull(t2);
        assertNotNull(t3);
        assertTrue(t1 instanceof PurchaseTransaction);
        assertTrue(t2 instanceof SaleTransaction);
        assertTrue(t3 instanceof RentalTransaction);
        assertEquals(95000000.0, t2.getTotalAmount());

        // Verificación de Reportes Gerenciales sobre el concesionario restaurado
        EmployeeReport report = loaded.generateEmployeeReport(activeEmp.getId(),
                Instant.now().minusSeconds(86400), Instant.now().plusSeconds(86400));
        assertNotNull(report);
        assertEquals(3, report.getTotalTransactionsCount());
        assertEquals(1, report.getPurchaseCount());
        assertEquals(1, report.getSaleCount());
        assertEquals(1, report.getRentalCount());
        assertEquals(95000000.0 * 0.02, report.getSalesCommission(), 0.001);
    }
}
