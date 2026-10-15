package com.omnidrive.model;

import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.TruckType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeavyVehiclesTest {

    @Test
    @DisplayName("Crea exitosamente una Van de pasajeros")
    void testVanCreation() throws Exception {
        Van van = new Van(
                "VAN123", "Mercedes-Benz", 2024, 6, 170.0, 2200.0,
                FuelType.DIESEL, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 210000000.0, null,
                true, null, null,
                12, 4, 4, true, true, true,
                900.0
        );

        assertEquals("VAN123", van.getPlate());
        assertEquals(12, van.getPassengerCount());
        assertEquals(900.0, van.getTrunkCapacityLiters());
    }

    @Test
    @DisplayName("Crea exitosamente un Bus con salidas de emergencia y ejes")
    void testBusCreation() throws Exception {
        Bus bus = new Bus(
                "BUS999", "Scania", 2023, 8, 120.0, 9000.0,
                FuelType.DIESEL, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 600000000.0, null,
                true, null, null,
                45, 2, 2, true, true, true,
                2500.0, 3, 4
        );

        assertEquals("BUS999", bus.getPlate());
        assertEquals(45, bus.getPassengerCount());
        assertEquals(3, bus.getAxleCount());
        assertEquals(4, bus.getEmergencyExitCount());
    }

    @Test
    @DisplayName("Rechaza un Bus con número de salidas de emergencia o ejes inválidos (<= 0)")
    void testInvalidBusParameters() {
        assertThrows(InvalidVehicleDataException.class, () -> new Bus(
                "BUS000", "Scania", 2023, 8, 120.0, 9000.0,
                FuelType.DIESEL, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 600000000.0, null,
                true, null, null,
                45, 2, 2, true, true, true,
                2500.0, 0, 4
        ));

        assertThrows(InvalidVehicleDataException.class, () -> new Bus(
                "BUS000", "Scania", 2023, 8, 120.0, 9000.0,
                FuelType.DIESEL, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 600000000.0, null,
                true, null, null,
                45, 2, 2, true, true, true,
                2500.0, 2, 0
        ));
    }

    @Test
    @DisplayName("Crea exitosamente un Camión con capacidad de carga, frenos de aire y tipo de camión")
    void testTruckCreation() throws Exception {
        Truck truck = new Truck(
                "TRK555", "Volvo", 2024, 12, 110.0, 12800.0,
                FuelType.DIESEL, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 480000000.0, null,
                true, null, null,
                28.0, true, true, true, 4, TruckType.BOX_TRUCK
        );

        assertEquals("TRK555", truck.getPlate());
        assertEquals(28.0, truck.getLoadCapacityTons());
        assertTrue(truck.hasAirBrakes());
        assertEquals(4, truck.getAxleCount());
        assertEquals(TruckType.BOX_TRUCK, truck.getTruckType());
    }

    @Test
    @DisplayName("Rechaza un Camión con capacidad de carga o ejes inválidos, o tipo nulo")
    void testInvalidTruckParameters() {
        assertThrows(InvalidVehicleDataException.class, () -> new Truck(
                "TRK000", "Volvo", 2024, 12, 110.0, 12800.0,
                FuelType.DIESEL, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 480000000.0, null,
                true, null, null,
                0.0, true, true, true, 4, TruckType.BOX_TRUCK
        ));

        assertThrows(InvalidVehicleDataException.class, () -> new Truck(
                "TRK000", "Volvo", 2024, 12, 110.0, 12800.0,
                FuelType.DIESEL, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 480000000.0, null,
                true, null, null,
                20.0, true, true, true, 0, TruckType.BOX_TRUCK
        ));

        assertThrows(InvalidVehicleDataException.class, () -> new Truck(
                "TRK000", "Volvo", 2024, 12, 110.0, 12800.0,
                FuelType.DIESEL, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 480000000.0, null,
                true, null, null,
                20.0, true, true, true, 3, null
        ));
    }
}
