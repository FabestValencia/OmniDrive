package com.omnidrive.model;

import com.omnidrive.exception.DuplicateVehicleException;
import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas del Contenedor Central Dealership y Filtros de Búsqueda")
class DealershipTest {

    private Dealership dealership;

    @BeforeEach
    void setUp() {
        dealership = new Dealership("OmniDrive Central");
    }

    private Sedan createSampleSedan(String plate, VehicleCondition condition, VehicleStatus status, double price, FuelType fuelType) throws Exception {
        return new Sedan(
                plate, "Toyota", 2023, 6, 210, 2000,
                fuelType, TransmissionType.AUTOMATIC, condition,
                status, price, null,
                true, null, null,
                5, 4, 6, true, true, true, 450, true, true, true, true
        );
    }

    private Motorcycle createSampleMotorcycle(String plate, VehicleCondition condition, VehicleStatus status, double price) throws Exception {
        return new Motorcycle(
                plate, "Yamaha", 2022, 5, 180, 600,
                FuelType.GASOLINE, TransmissionType.MANUAL, condition,
                status, price, null,
                true, null, null
        );
    }

    @Test
    @DisplayName("Debe registrar un vehículo con placa única exitosamente")
    void testRegisterVehicleSuccessfully() throws Exception {
        Sedan sedan = createSampleSedan("ABC123", VehicleCondition.NEW, VehicleStatus.AVAILABLE_FOR_SALE, 25000, FuelType.GASOLINE);

        dealership.registerVehicle(sedan);

        assertEquals(1, dealership.getVehicleCount());
        assertTrue(dealership.containsVehicle("ABC123"));
        assertEquals(sedan, dealership.findVehicleByPlate("abc123"));
    }

    @Test
    @DisplayName("Debe rechazar registro de vehículo con placa duplicada")
    void testRejectDuplicatePlate() throws Exception {
        Sedan sedan1 = createSampleSedan("ABC123", VehicleCondition.NEW, VehicleStatus.AVAILABLE_FOR_SALE, 25000, FuelType.GASOLINE);
        Motorcycle moto = createSampleMotorcycle("ABC123", VehicleCondition.NEW, VehicleStatus.AVAILABLE_FOR_SALE, 12000);

        dealership.registerVehicle(sedan1);

        DuplicateVehicleException ex = assertThrows(DuplicateVehicleException.class, () -> dealership.registerVehicle(moto));
        assertTrue(ex.getMessage().contains("ABC123"));
    }

    @Test
    @DisplayName("Debe rechazar el registro de un vehículo nulo")
    void testRejectNullVehicle() {
        assertThrows(InvalidVehicleDataException.class, () -> dealership.registerVehicle(null));
    }

    @Test
    @DisplayName("Debe rechazar registro con estado inicial distinto a disponible para venta o alquiler")
    void testRejectInvalidInitialStatus() throws Exception {
        Sedan sedan = createSampleSedan("VND001", VehicleCondition.NEW, VehicleStatus.SOLD, 30000, FuelType.GASOLINE);

        InvalidVehicleDataException ex = assertThrows(InvalidVehicleDataException.class, () -> dealership.registerVehicle(sedan));
        assertTrue(ex.getMessage().toLowerCase().contains("inicial"));
    }

    @Test
    @DisplayName("Debe filtrar vehículos por tipo, combustible, condición, rango de precio y disponibilidad")
    void testFilterVehicles() throws Exception {
        Sedan sedanNew = createSampleSedan("SED001", VehicleCondition.NEW, VehicleStatus.AVAILABLE_FOR_SALE, 30000, FuelType.GASOLINE);
        Sedan sedanUsed = createSampleSedan("SED002", VehicleCondition.USED, VehicleStatus.AVAILABLE_FOR_RENT, 15000, FuelType.DIESEL);
        Motorcycle moto = createSampleMotorcycle("MOT001", VehicleCondition.NEW, VehicleStatus.AVAILABLE_FOR_SALE, 8000);

        dealership.registerVehicle(sedanNew);
        dealership.registerVehicle(sedanUsed);
        dealership.registerVehicle(moto);

        // Filtro por tipo Sedan
        VehicleFilter filterByType = new VehicleFilter.Builder()
                .vehicleType(Sedan.class)
                .build();
        List<Vehicle> sedans = dealership.filterVehicles(filterByType);
        assertEquals(2, sedans.size());

        // Filtro por combustible DIESEL
        VehicleFilter filterByFuel = new VehicleFilter.Builder()
                .fuelType(FuelType.DIESEL)
                .build();
        List<Vehicle> diesels = dealership.filterVehicles(filterByFuel);
        assertEquals(1, diesels.size());
        assertEquals("SED002", diesels.get(0).getPlate());

        // Filtro por rango de precio (10,000 a 20,000)
        VehicleFilter filterByPrice = new VehicleFilter.Builder()
                .minPrice(10000.0)
                .maxPrice(20000.0)
                .build();
        List<Vehicle> midPrice = dealership.filterVehicles(filterByPrice);
        assertEquals(1, midPrice.size());
        assertEquals("SED002", midPrice.get(0).getPlate());

        // Filtro por estado AVAILABLE_FOR_SALE
        VehicleFilter filterByStatus = new VehicleFilter.Builder()
                .status(VehicleStatus.AVAILABLE_FOR_SALE)
                .build();
        List<Vehicle> forSale = dealership.filterVehicles(filterByStatus);
        assertEquals(2, forSale.size());

        // Filtro combinado: Sedan + NEW + AVAILABLE_FOR_SALE
        VehicleFilter combinedFilter = new VehicleFilter.Builder()
                .vehicleType(Sedan.class)
                .condition(VehicleCondition.NEW)
                .status(VehicleStatus.AVAILABLE_FOR_SALE)
                .build();
        List<Vehicle> combinedResults = dealership.filterVehicles(combinedFilter);
        assertEquals(1, combinedResults.size());
        assertEquals("SED001", combinedResults.get(0).getPlate());
    }

    @Test
    @DisplayName("Debe devolver todos los vehículos cuando el filtro es nulo o vacío")
    void testFilterNullReturnsAll() throws Exception {
        Sedan sedan = createSampleSedan("ALL001", VehicleCondition.NEW, VehicleStatus.AVAILABLE_FOR_SALE, 25000, FuelType.GASOLINE);
        dealership.registerVehicle(sedan);

        assertEquals(1, dealership.filterVehicles(null).size());
        assertEquals(1, dealership.filterVehicles(new VehicleFilter.Builder().build()).size());
    }

    @Test
    @DisplayName("Debe permitir remover un vehículo por placa")
    void testRemoveVehicle() throws Exception {
        Sedan sedan = createSampleSedan("DEL001", VehicleCondition.NEW, VehicleStatus.AVAILABLE_FOR_SALE, 25000, FuelType.GASOLINE);
        dealership.registerVehicle(sedan);

        boolean removed = dealership.removeVehicle("DEL001");
        assertTrue(removed);
        assertEquals(0, dealership.getVehicleCount());
        assertFalse(dealership.containsVehicle("DEL001"));
    }
}
