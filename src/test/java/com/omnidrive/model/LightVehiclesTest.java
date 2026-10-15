package com.omnidrive.model;

import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LightVehiclesTest {

    @Test
    @DisplayName("Crea exitosamente una Moto con sus atributos básicos")
    void testMotorcycleCreation() throws Exception {
        Motorcycle moto = new Motorcycle(
                "MOT001", "Yamaha", 2023, 6, 180.0, 321.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 28000000.0, null,
                true, null, null);

        assertEquals("MOT001", moto.getPlate());
        assertEquals("Yamaha", moto.getBrand());
        assertEquals(321.0, moto.getCylinderCapacityCc());
        assertEquals(FuelType.GASOLINE, moto.getFuelType());
    }

    @Test
    @DisplayName("Crea exitosamente una Moto eléctrica con ElectricDetails")
    void testElectricMotorcycleCreation() throws Exception {
        ElectricDetails electric = new ElectricDetails(120.0, 3.0);
        Motorcycle moto = new Motorcycle(
                "MOT99E", "Zero", 2024, 1, 140.0, 0.0,
                FuelType.ELECTRIC, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 45000000.0, null,
                true, electric, null);

        assertNotNull(moto.getElectricDetails());
        assertEquals(120.0, moto.getElectricDetails().getAutonomyKm());
    }

    @Test
    @DisplayName("Crea exitosamente un Deportivo con atributos de rendimiento (HP y 0-100 km/h)")
    void testSportsCarCreation() throws Exception {
        SportsCar sportsCar = new SportsCar(
                "DEP100", "Porsche", 2024, 7, 305.0, 3000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 450000000.0, null,
                true, null, null,
                2, 2, 6, 450, 3.8);

        assertEquals("DEP100", sportsCar.getPlate());
        assertEquals(450, sportsCar.getHorsepower());
        assertEquals(3.8, sportsCar.getZeroToHundredSecs());
        assertEquals(2, sportsCar.getPassengerCount());
        assertEquals(6, sportsCar.getAirbagsCount());
    }

    @Test
    @DisplayName("Rechaza un Deportivo con caballos de fuerza o tiempo 0-100 negativos o cero")
    void testInvalidSportsCarPerformance() {
        assertThrows(InvalidVehicleDataException.class, () -> new SportsCar(
                "DEP000", "Porsche", 2024, 7, 305.0, 3000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 450000000.0, null,
                true, null, null,
                2, 2, 6, 0, 3.8));

        assertThrows(InvalidVehicleDataException.class, () -> new SportsCar(
                "DEP000", "Porsche", 2024, 7, 305.0, 3000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 450000000.0, null,
                true, null, null,
                2, 2, 6, 450, 0.0));
    }
}
