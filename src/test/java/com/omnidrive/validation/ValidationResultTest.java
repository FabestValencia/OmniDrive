package com.omnidrive.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Contenedor ValidationResult (T1 - Spec 007)")
class ValidationResultTest {

    @Test
    @DisplayName("Instancia success() representa un resultado válido sin errores")
    void testSuccessResult() {
        ValidationResult result = ValidationResult.success();

        assertTrue(result.isValid());
        assertFalse(result.isInvalid());
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
        assertEquals("", result.getFirstErrorMessage());
        assertEquals("", result.getCombinedErrorMessage());
    }

    @Test
    @DisplayName("Instancia failure con un mensaje almacena el error y marca invalidez")
    void testSingleFailureMessage() {
        ValidationResult result = ValidationResult.failure("El correo no es válido.");

        assertFalse(result.isValid());
        assertTrue(result.isInvalid());
        assertEquals(1, result.getErrors().size());
        assertEquals("El correo no es válido.", result.getFirstErrorMessage());
        assertEquals("El correo no es válido.", result.getCombinedErrorMessage());
    }

    @Test
    @DisplayName("Instancia failure con múltiples mensajes varargs preserva todos los errores")
    void testMultipleFailureMessagesVarargs() {
        ValidationResult result = ValidationResult.failure("Error 1", "Error 2", "Error 3");

        assertFalse(result.isValid());
        assertEquals(3, result.getErrors().size());
        assertEquals("Error 1", result.getFirstErrorMessage());
        assertTrue(result.getCombinedErrorMessage().contains("Error 1"));
        assertTrue(result.getCombinedErrorMessage().contains("Error 2"));
        assertTrue(result.getCombinedErrorMessage().contains("Error 3"));
    }

    @Test
    @DisplayName("Instancia failure con lista preserva orden y es inmutable")
    void testFailureListImmutability() {
        List<String> errors = List.of("Placa inválida", "Teléfono muy corto");
        ValidationResult result = ValidationResult.failure(errors);

        assertFalse(result.isValid());
        assertEquals(2, result.getErrors().size());
        assertEquals("Placa inválida", result.getFirstErrorMessage());

        assertThrows(UnsupportedOperationException.class, () -> result.getErrors().add("Nuevo error"));
    }

    @Test
    @DisplayName("Fábrica failure maneja parámetros nulos o vacíos defensivamente")
    void testFailureNullHandling() {
        ValidationResult nullMsgResult = ValidationResult.failure((String) null);
        assertFalse(nullMsgResult.isValid());
        assertEquals(1, nullMsgResult.getErrors().size());
        assertEquals("Dato inválido.", nullMsgResult.getFirstErrorMessage());

        ValidationResult nullListResult = ValidationResult.failure((List<String>) null);
        assertFalse(nullListResult.isValid());
        assertEquals(1, nullListResult.getErrors().size());
    }
}
