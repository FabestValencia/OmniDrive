package com.omnidrive.model;

import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FamilyVehiclesTest {

    @Test
    @DisplayName("Crea exitosamente un Sedán con todos sus asistentes y sensores")
    void testSedanCreation() throws Exception {
        Sedan sedan = new Sedan(
                "SED123", "Honda", 2024, 6, 210.0, 1500.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 95000000.0, null,
                true, null, null,
                5, 4, 6, true, true, true,
                450.0, true, true, true, true
        );

        assertEquals("SED123", sedan.getPlate());
        assertEquals(5, sedan.getPassengerCount());
        assertEquals(450.0, sedan.getTrunkCapacityLiters());
        assertTrue(sedan.hasCruiseControl());
        assertTrue(sedan.hasCollisionSensors());
        assertTrue(sedan.hasCrossTrafficAlert());
        assertTrue(sedan.hasLaneKeepingAssist());
    }

    @Test
    @DisplayName("Rechaza un Sedán con capacidad de maletero inválida (<= 0)")
    void testInvalidSedanTrunkCapacity() {
        assertThrows(InvalidVehicleDataException.class, () -> new Sedan(
                "SED000", "Honda", 2024, 6, 210.0, 1500.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 95000000.0, null,
                true, null, null,
                5, 4, 6, true, true, true,
                0.0, true, true, true, true
        ));
    }

    @Test
    @DisplayName("Crea exitosamente una Camioneta/SUV con tracción 4x4")
    void testSuvCreation() throws Exception {
        Suv suv = new Suv(
                "SUV444", "Ford", 2023, 8, 220.0, 2300.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 160000000.0, null,
                true, null, null,
                7, 5, 8, true, true, true,
                600.0, true, true, true, true, true
        );

        assertEquals("SUV444", suv.getPlate());
        assertEquals(7, suv.getPassengerCount());
        assertEquals(600.0, suv.getTrunkCapacityLiters());
        assertTrue(suv.isFourByFour());
    }

    @Test
    @DisplayName("Crea exitosamente una Pick Up con capacidad de caja de carga")
    void testPickUpCreation() throws Exception {
        PickUp pickUp = new PickUp(
                "PCK789", "Toyota", 2023, 6, 180.0, 2800.0,
                FuelType.DIESEL, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 175000000.0, null,
                true, null, null,
                5, 4, 6, true, true, true,
                true, 1100.0
        );

        assertEquals("PCK789", pickUp.getPlate());
        assertTrue(pickUp.isFourByFour());
        assertEquals(1100.0, pickUp.getCargoBedCapacityKg());
    }

    @Test
    @DisplayName("Rechaza una Pick Up con capacidad de carga inválida (<= 0)")
    void testInvalidPickUpCargoCapacity() {
        assertThrows(InvalidVehicleDataException.class, () -> new PickUp(
                "PCK000", "Toyota", 2023, 6, 180.0, 2800.0,
                FuelType.DIESEL, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 175000000.0, null,
                true, null, null,
                5, 4, 6, true, true, true,
                true, -50.0
        ));
    }
}
