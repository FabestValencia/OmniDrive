package com.omnidrive;

import com.omnidrive.exception.OmniDriveException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    @Test
    @DisplayName("Verifica que la clase principal de JavaFX esté disponible")
    void testAppClassExists() {
        App app = new App();
        assertNotNull(app, "La instancia de App no debe ser nula");
    }

    @Test
    @DisplayName("Verificación básica del entorno de pruebas JUnit 5")
    void testBasicSanity() {
        assertTrue(true, "El entorno de ejecución de pruebas JUnit 5 funciona correctamente");
    }

    @Test
    @DisplayName("Verifica la instanciación y captura de la excepción base OmniDriveException")
    void testOmniDriveException() {
        String errorMessage = "Falla de regla de negocio en OmniDrive";
        OmniDriveException exception = assertThrows(
                OmniDriveException.class,
                () -> {
                    throw new OmniDriveException(errorMessage);
                },
                "Debe lanzar OmniDriveException"
        );

        assertEquals(errorMessage, exception.getMessage());
        assertNotNull(exception);
    }
}
