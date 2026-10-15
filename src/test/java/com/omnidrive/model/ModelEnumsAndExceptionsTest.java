package com.omnidrive.model;

import com.omnidrive.exception.DuplicateVehicleException;
import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.exception.OmniDriveException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.model.enums.FuelType;
import com.omnidrive.model.enums.TransmissionType;
import com.omnidrive.model.enums.TruckType;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelEnumsAndExceptionsTest {

    @Test
    @DisplayName("Verifica la existencia y valores del enum FuelType")
    void testFuelTypeValues() {
        assertEquals(4, FuelType.values().length);
        assertNotNull(FuelType.valueOf("GASOLINE"));
        assertNotNull(FuelType.valueOf("DIESEL"));
        assertNotNull(FuelType.valueOf("ELECTRIC"));
        assertNotNull(FuelType.valueOf("HYBRID"));
    }

    @Test
    @DisplayName("Verifica la existencia y valores del enum TransmissionType")
    void testTransmissionTypeValues() {
        assertEquals(2, TransmissionType.values().length);
        assertNotNull(TransmissionType.valueOf("MANUAL"));
        assertNotNull(TransmissionType.valueOf("AUTOMATIC"));
    }

    @Test
    @DisplayName("Verifica la existencia y valores del enum VehicleCondition")
    void testVehicleConditionValues() {
        assertEquals(2, VehicleCondition.values().length);
        assertNotNull(VehicleCondition.valueOf("NEW"));
        assertNotNull(VehicleCondition.valueOf("USED"));
    }

    @Test
    @DisplayName("Verifica la existencia y valores del enum VehicleStatus")
    void testVehicleStatusValues() {
        assertEquals(5, VehicleStatus.values().length);
        assertNotNull(VehicleStatus.valueOf("AVAILABLE_FOR_SALE"));
        assertNotNull(VehicleStatus.valueOf("AVAILABLE_FOR_RENT"));
        assertNotNull(VehicleStatus.valueOf("SOLD"));
        assertNotNull(VehicleStatus.valueOf("RENTED"));
        assertNotNull(VehicleStatus.valueOf("MAINTENANCE"));
    }

    @Test
    @DisplayName("Verifica la existencia y valores del enum TruckType")
    void testTruckTypeValues() {
        assertEquals(4, TruckType.values().length);
        assertNotNull(TruckType.valueOf("BOX_TRUCK"));
        assertNotNull(TruckType.valueOf("TANKER"));
        assertNotNull(TruckType.valueOf("HOPPER"));
        assertNotNull(TruckType.valueOf("FLATBED"));
    }

    @Test
    @DisplayName("Verifica que las excepciones heredan de OmniDriveException y son serializables")
    void testExceptionsInheritanceAndSerialization() throws Exception {
        DuplicateVehicleException dupEx = new DuplicateVehicleException("Placa duplicada");
        assertInstanceOf(OmniDriveException.class, dupEx);
        assertInstanceOf(Serializable.class, dupEx);

        TechnicalReviewRejectedException techEx = new TechnicalReviewRejectedException("Revisión rechazada");
        assertInstanceOf(OmniDriveException.class, techEx);
        assertInstanceOf(Serializable.class, techEx);

        InvalidVehicleDataException invEx = new InvalidVehicleDataException("Datos inválidos");
        assertInstanceOf(OmniDriveException.class, invEx);
        assertInstanceOf(Serializable.class, invEx);

        // Prueba de serialización real en bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(techEx);
        }

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            Object deserialized = ois.readObject();
            assertTrue(deserialized instanceof TechnicalReviewRejectedException);
            assertEquals("Revisión rechazada", ((TechnicalReviewRejectedException) deserialized).getMessage());
        }
    }
}
