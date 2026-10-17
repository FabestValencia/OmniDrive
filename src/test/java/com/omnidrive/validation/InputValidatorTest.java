package com.omnidrive.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Motor InputValidator (T2 - Spec 007)")
class InputValidatorTest {

    @Test
    @DisplayName("Validación de placas acepta formatos estándar de automóviles y motocicletas")
    void testValidatePlateValid() {
        String[] validPlates = {"ABC-123", "ABC123", "abc-123", "XYZ-45D", "XYZ45D", "mot-99k"};
        for (String plate : validPlates) {
            ValidationResult result = InputValidator.validatePlate(plate);
            assertTrue(result.isValid(), "La placa '" + plate + "' debería ser válida");
        }
    }

    @Test
    @DisplayName("Validación de placas rechaza formatos no conformes")
    void testValidatePlateInvalid() {
        String[] invalidPlates = {"", "   ", "A-123", "123-ABC", "ABCD-12", "AB-1234", "ABC-12345"};
        for (String plate : invalidPlates) {
            ValidationResult result = InputValidator.validatePlate(plate);
            assertTrue(result.isInvalid(), "La placa '" + plate + "' debería ser inválida");
            assertFalse(result.getFirstErrorMessage().isEmpty());
        }
    }

    @Test
    @DisplayName("Validación de cédula o identificación acepta cadenas válidas y rechaza vacías o cortas")
    void testValidateId() {
        assertTrue(InputValidator.validateId("12345678").isValid());
        assertTrue(InputValidator.validateId("EMP-001").isValid());

        assertFalse(InputValidator.validateId(null).isValid());
        assertFalse(InputValidator.validateId("").isValid());
        assertFalse(InputValidator.validateId("12").isValid()); // Muy corta (< 4)
    }

    @Test
    @DisplayName("Validación de nombres exige no vacíos y longitud mínima")
    void testValidateName() {
        assertTrue(InputValidator.validateName("Carlos", "Nombre").isValid());
        assertFalse(InputValidator.validateName(null, "Nombre").isValid());
        assertFalse(InputValidator.validateName("", "Nombre").isValid());
        assertFalse(InputValidator.validateName("A", "Nombre").isValid());
    }

    @Test
    @DisplayName("Validación de correo electrónico verifica formato RFC simplificado")
    void testValidateEmail() {
        assertTrue(InputValidator.validateEmail("usuario@omnidrive.com").isValid());
        assertTrue(InputValidator.validateEmail("test.user+tag@gmail.co").isValid());

        assertFalse(InputValidator.validateEmail(null).isValid());
        assertFalse(InputValidator.validateEmail("invalido").isValid());
        assertFalse(InputValidator.validateEmail("usuario@.com").isValid());
        assertFalse(InputValidator.validateEmail("usuario@com").isValid());
    }

    @Test
    @DisplayName("Validación de teléfono limpia caracteres y valida longitud numérica entre 7 y 12 dígitos")
    void testValidatePhone() {
        assertTrue(InputValidator.validatePhone("3001234567").isValid());
        assertTrue(InputValidator.validatePhone("300-123-4567").isValid());
        assertTrue(InputValidator.validatePhone("300 123 4567").isValid());
        assertTrue(InputValidator.validatePhone("7654321").isValid());

        assertFalse(InputValidator.validatePhone(null).isValid());
        assertFalse(InputValidator.validatePhone("12345").isValid()); // Muy corto
        assertFalse(InputValidator.validatePhone("30012345678999").isValid()); // Muy largo
        assertFalse(InputValidator.validatePhone("300ABC4567").isValid());
    }

    @Test
    @DisplayName("Validación de año de modelo exige rango coherente entre 1900 y año actual + 1")
    void testValidateModelYear() {
        int currentYear = Year.now().getValue();
        assertTrue(InputValidator.validateModelYear(2024).isValid());
        assertTrue(InputValidator.validateModelYear(currentYear).isValid());
        assertTrue(InputValidator.validateModelYear(currentYear + 1).isValid());

        assertFalse(InputValidator.validateModelYear(1899).isValid());
        assertFalse(InputValidator.validateModelYear(currentYear + 2).isValid());
    }

    @Test
    @DisplayName("Parseo de entero positivo convierte valores sin lanzar NumberFormatException")
    void testParsePositiveInt() {
        assertTrue(InputValidator.parsePositiveInt("5", "Pasajeros").isValid());
        assertTrue(InputValidator.parsePositiveInt(" 10 ", "Velocidades").isValid());

        ValidationResult zero = InputValidator.parsePositiveInt("0", "Pasajeros");
        assertFalse(zero.isValid());
        assertTrue(zero.getFirstErrorMessage().contains("mayor a 0"));

        ValidationResult negative = InputValidator.parsePositiveInt("-4", "Pasajeros");
        assertFalse(negative.isValid());

        ValidationResult nonNumeric = InputValidator.parsePositiveInt("abc", "Pasajeros");
        assertFalse(nonNumeric.isValid());
        assertTrue(nonNumeric.getFirstErrorMessage().contains("número entero válido"));
    }

    @Test
    @DisplayName("Parseo de decimal positivo maneja comas o puntos decimales de forma segura")
    void testParsePositiveDouble() {
        assertTrue(InputValidator.parsePositiveDouble("50000.50", "Precio").isValid());
        assertTrue(InputValidator.parsePositiveDouble("50000,50", "Precio").isValid());

        assertFalse(InputValidator.parsePositiveDouble("0", "Precio").isValid());
        assertFalse(InputValidator.parsePositiveDouble("-100", "Precio").isValid());

        ValidationResult nonNumber = InputValidator.parsePositiveDouble("cien mil", "Precio");
        assertFalse(nonNumber.isValid());
        assertTrue(nonNumber.getFirstErrorMessage().contains("número decimal válido"));
    }

    @Test
    @DisplayName("Validación de rango de fechas detecta nulidad y cronología invertida")
    void testValidateDateRange() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        assertTrue(InputValidator.validateDateRange(start, end).isValid());
        assertTrue(InputValidator.validateDateRange(start, start).isValid()); // Mismo día permitido

        ValidationResult inverted = InputValidator.validateDateRange(end, start);
        assertFalse(inverted.isValid());
        assertTrue(inverted.getFirstErrorMessage().contains("no puede ser posterior"));

        assertFalse(InputValidator.validateDateRange(null, end).isValid());
        assertFalse(InputValidator.validateDateRange(start, null).isValid());
    }
}
