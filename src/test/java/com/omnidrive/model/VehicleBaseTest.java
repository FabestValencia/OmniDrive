package com.omnidrive.model;

import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleBaseTest {

    // Subclase anónima concreta para probar la clase abstracta Vehicle
    private static class ConcreteTestVehicle extends Vehicle {
        private static final long serialVersionUID = 1L;

        public ConcreteTestVehicle(String plate, String brand, int modelYear, int speedCount,
                double maxSpeedKmH, double cylinderCapacityCc, FuelType fuelType,
                TransmissionType transmissionType, VehicleCondition condition,
                VehicleStatus status, double basePrice, String photoUrl,
                boolean isTechnicalReviewApproved, ElectricDetails electricDetails,
                HybridDetails hybridDetails) throws InvalidVehicleDataException, TechnicalReviewRejectedException {
            super(plate, brand, modelYear, speedCount, maxSpeedKmH, cylinderCapacityCc,
                    fuelType, transmissionType, condition, status, basePrice, photoUrl,
                    isTechnicalReviewApproved, electricDetails, hybridDetails);
        }
    }

    @Test
    @DisplayName("Crea exitosamente un vehículo con datos válidos")
    void testValidVehicleCreation() throws Exception {
        ConcreteTestVehicle vehicle = new ConcreteTestVehicle(
                "abc123", "Toyota", 2023, 6, 200.0, 1800.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 85000000.0, null,
                true, null, null);

        assertEquals("ABC123", vehicle.getPlate(), "La placa debe normalizarse a mayúsculas");
        assertEquals("Toyota", vehicle.getBrand());
        assertEquals(Vehicle.DEFAULT_PHOTO_URL, vehicle.getPhotoUrl(),
                "Debe asignar la foto por defecto si no se pasa una");
        assertEquals(VehicleStatus.AVAILABLE_FOR_SALE, vehicle.getStatus());
    }

    @Test
    @DisplayName("Rechaza la creación si la placa es nula o vacía")
    void testBlankPlateRejection() {
        assertThrows(InvalidVehicleDataException.class, () -> new ConcreteTestVehicle(
                "", "Toyota", 2023, 6, 200.0, 1800.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 85000000.0, null,
                true, null, null));
    }

    @Test
    @DisplayName("Rechaza vehículo usado si la revisión técnica fue rechazada (isTechnicalReviewApproved == false)")
    void testUsedVehicleWithoutTechnicalReviewThrowsException() {
        assertThrows(TechnicalReviewRejectedException.class, () -> new ConcreteTestVehicle(
                "USD123", "Chevrolet", 2018, 5, 170.0, 1400.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.USED,
                VehicleStatus.AVAILABLE_FOR_SALE, 35000000.0, null,
                false, null, null));
    }

    @Test
    @DisplayName("Permite vehículo usado si la revisión técnica fue aprobada")
    void testUsedVehicleWithTechnicalReviewApproved() throws Exception {
        ConcreteTestVehicle vehicle = new ConcreteTestVehicle(
                "USD123", "Chevrolet", 2018, 5, 170.0, 1400.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.USED,
                VehicleStatus.AVAILABLE_FOR_SALE, 35000000.0, null,
                true, null, null);
        assertTrue(vehicle.isTechnicalReviewApproved());
        assertEquals(VehicleCondition.USED, vehicle.getCondition());
    }

    @Test
    @DisplayName("Exige ElectricDetails cuando el combustible es ELECTRIC")
    void testElectricFuelRequiresDetails() throws Exception {
        assertThrows(InvalidVehicleDataException.class, () -> new ConcreteTestVehicle(
                "ELE001", "Tesla", 2024, 1, 240.0, 0.0,
                FuelType.ELECTRIC, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 180000000.0, null,
                true, null, null));

        // Con detalles válidos debe crearse correctamente
        ElectricDetails electricDetails = new ElectricDetails(480.0, 1.5);
        assertEquals(480.0, electricDetails.getAutonomyKm());
        assertEquals(1.5, electricDetails.getAverageChargingHours());
    }

    @Test
    @DisplayName("Exige HybridDetails cuando el combustible es HYBRID")
    void testHybridFuelRequiresDetails() throws Exception {
        assertThrows(InvalidVehicleDataException.class, () -> new ConcreteTestVehicle(
                "HYB001", "Toyota", 2024, 6, 190.0, 1800.0,
                FuelType.HYBRID, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 120000000.0, null,
                true, null, null));

        HybridDetails hybridDetails = new HybridDetails(true, false);
        assertTrue(hybridDetails.isPlugIn());
        assertFalse(hybridDetails.isMildHybrid());
    }

    @Test
    @DisplayName("Rechaza valores numéricos inconsistentes (precio <= 0, velocidades <= 0)")
    void testInconsistentNumericalValues() {
        assertThrows(InvalidVehicleDataException.class, () -> new ConcreteTestVehicle(
                "ERR001", "Mazda", 2022, 0, 180.0, 1600.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 50000000.0, null,
                true, null, null));

        assertThrows(InvalidVehicleDataException.class, () -> new ConcreteTestVehicle(
                "ERR002", "Mazda", 2022, 5, 180.0, 1600.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, -100.0, null,
                true, null, null));
    }
}
