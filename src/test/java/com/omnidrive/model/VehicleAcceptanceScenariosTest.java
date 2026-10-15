package com.omnidrive.model;

import com.omnidrive.exception.DuplicateVehicleException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Escenarios de Aceptación Oficiales - Feature 001-vehicle-hierarchy")
class VehicleAcceptanceScenariosTest {

    private Dealership dealership;

    @BeforeEach
    void setUp() {
        dealership = new Dealership("OmniDrive HQ");
    }

    @Test
    @DisplayName("Escenario 1: Registro exitoso de vehículo nuevo (Sedán Gasolina)")
    void testScenario1_SuccessfulRegistrationNewSedan() throws Exception {
        // Dado que se ingresan los datos válidos de un Sedán con placa única "ABC123",
        // nuevo, combustible Gasolina y destino "Disponible para venta"
        Sedan sedan = new Sedan(
                "ABC123", "Mazda", 2024, 6, 220, 2000,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 28000, null,
                true, null, null,
                5, 4, 6, true, true, true, 480, true, true, true, true
        );

        // Cuando se solicita el registro en el inventario
        dealership.registerVehicle(sedan);

        // Entonces el vehículo queda guardado exitosamente con estado "Disponible para venta"
        Vehicle savedVehicle = dealership.findVehicleByPlate("ABC123");
        assertNotNull(savedVehicle);
        assertEquals("ABC123", savedVehicle.getPlate());
        assertEquals(VehicleStatus.AVAILABLE_FOR_SALE, savedVehicle.getStatus());
        assertEquals(VehicleCondition.NEW, savedVehicle.getCondition());
        assertEquals(FuelType.GASOLINE, savedVehicle.getFuelType());
    }

    @Test
    @DisplayName("Escenario 2: Registro exitoso de vehículo eléctrico (Deportivo)")
    void testScenario2_SuccessfulRegistrationElectricSportsCar() throws Exception {
        // Dado que se ingresan los datos de un Deportivo con placa única "ELE999",
        // combustible Eléctrico, autonomía de 450 km y tiempo de carga de 2 horas
        ElectricDetails electricDetails = new ElectricDetails(450.0, 2.0);
        SportsCar sportsCar = new SportsCar(
                "ELE999", "Porsche", 2024, 2, 260, 0,
                FuelType.ELECTRIC, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 120000, null,
                true, electricDetails, null,
                2, 2, 6, 600, 2.8
        );

        // Cuando se solicita el registro
        dealership.registerVehicle(sportsCar);

        // Entonces el sistema valida los parámetros eléctricos y registra el vehículo satisfactoriamente
        Vehicle savedVehicle = dealership.findVehicleByPlate("ELE999");
        assertNotNull(savedVehicle);
        assertEquals(FuelType.ELECTRIC, savedVehicle.getFuelType());
        assertNotNull(savedVehicle.getElectricDetails());
        assertEquals(450.0, savedVehicle.getElectricDetails().getAutonomyKm());
        assertEquals(2.0, savedVehicle.getElectricDetails().getAverageChargingHours());
    }

    @Test
    @DisplayName("Escenario 3: Rechazo de registro de vehículo usado sin revisión técnica aprobada")
    void testScenario3_RejectUsedVehicleWithoutApprovedTechnicalReview() {
        // Dado que se intenta registrar una Camioneta usada con placa "USD456" y la revisión técnica previa no aprobada
        // Cuando se procesa la solicitud / creación
        // Entonces el sistema no almacena el vehículo y genera una alerta de rechazo por revisión técnica no aprobada
        TechnicalReviewRejectedException ex = assertThrows(TechnicalReviewRejectedException.class, () -> new Suv(
                "USD456", "Toyota", 2020, 6, 190, 2500,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.USED,
                VehicleStatus.AVAILABLE_FOR_SALE, 22000, null,
                false, null, null, // revisión técnica rechazada
                5, 5, 6, true, true, true, 500, true, true, true, true, true
        ));

        assertTrue(ex.getMessage().contains("USD456"));
        assertFalse(dealership.containsVehicle("USD456"));
    }

    @Test
    @DisplayName("Escenario 4: Rechazo por placa duplicada")
    void testScenario4_RejectDuplicatePlate() throws Exception {
        // Dado que ya existe en el inventario un vehículo con placa "DUP001"
        Sedan firstSedan = new Sedan(
                "DUP001", "Honda", 2023, 6, 205, 1800,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 24000, null,
                true, null, null,
                5, 4, 6, true, true, true, 420, true, true, true, true
        );
        dealership.registerVehicle(firstSedan);

        // Cuando se intenta registrar otro vehículo con la misma placa "DUP001"
        Motorcycle duplicateMoto = new Motorcycle(
                "DUP001", "Yamaha", 2023, 5, 170, 400,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 7000, null,
                true, null, null
        );

        // Entonces el sistema rechaza la operación e indica que la placa ya se encuentra registrada
        DuplicateVehicleException ex = assertThrows(DuplicateVehicleException.class, () -> dealership.registerVehicle(duplicateMoto));
        assertTrue(ex.getMessage().contains("DUP001"));
        assertEquals(1, dealership.getVehicleCount());
    }

    @Test
    @DisplayName("Escenario 5: Manejo de foto por defecto")
    void testScenario5_DefaultPhotoWhenNoneProvided() throws Exception {
        // Dado que se registra una Moto sin especificar una ruta de foto
        Motorcycle moto = new Motorcycle(
                "DEF777", "Suzuki", 2022, 5, 150, 250,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 4500, null, // Sin foto personalizada
                true, null, null
        );

        // Cuando se completa el registro
        dealership.registerVehicle(moto);

        // Entonces el vehículo queda asociado a la imagen genérica predeterminada del sistema
        Vehicle savedVehicle = dealership.findVehicleByPlate("DEF777");
        assertNotNull(savedVehicle);
        assertEquals(Vehicle.DEFAULT_PHOTO_URL, savedVehicle.getPhotoUrl());
        assertTrue(savedVehicle.getPhotoUrl().endsWith("default-vehicle.png"));
    }
}
