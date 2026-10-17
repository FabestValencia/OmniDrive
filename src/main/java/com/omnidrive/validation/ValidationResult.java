package com.omnidrive.validation;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Contenedor inmutable que encapsula el resultado de una operación de validación de datos.
 * Desacoplado de cualquier framework visual, permite evaluar el estado de validez
 * y recuperar los mensajes de error asociados en español.
 */
public final class ValidationResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_ERROR_MESSAGE = "Dato inválido.";
    private static final ValidationResult SUCCESS_INSTANCE = new ValidationResult(true, Collections.emptyList());

    private final boolean valid;
    private final List<String> errors;

    private ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        if (errors == null || errors.isEmpty()) {
            this.errors = Collections.emptyList();
        } else {
            List<String> cleaned = new ArrayList<>();
            for (String err : errors) {
                if (err != null && !err.trim().isEmpty()) {
                    cleaned.add(err.trim());
                } else if (err == null && !valid) {
                    cleaned.add(DEFAULT_ERROR_MESSAGE);
                }
            }
            if (cleaned.isEmpty() && !valid) {
                cleaned.add(DEFAULT_ERROR_MESSAGE);
            }
            this.errors = Collections.unmodifiableList(cleaned);
        }
    }

    /**
     * Retorna una instancia compartida que representa una validación exitosa sin errores.
     */
    public static ValidationResult success() {
        return SUCCESS_INSTANCE;
    }

    /**
     * Construye un resultado fallido con un mensaje de error específico.
     *
     * @param errorMessage Mensaje explicativo del error.
     */
    public static ValidationResult failure(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            return new ValidationResult(false, List.of(DEFAULT_ERROR_MESSAGE));
        }
        return new ValidationResult(false, List.of(errorMessage.trim()));
    }

    /**
     * Construye un resultado fallido con múltiples mensajes de error.
     */
    public static ValidationResult failure(String... errorMessages) {
        if (errorMessages == null || errorMessages.length == 0) {
            return new ValidationResult(false, List.of(DEFAULT_ERROR_MESSAGE));
        }
        return new ValidationResult(false, Arrays.asList(errorMessages));
    }

    /**
     * Construye un resultado fallido a partir de una lista de errores.
     */
    public static ValidationResult failure(List<String> errorMessages) {
        if (errorMessages == null || errorMessages.isEmpty()) {
            return new ValidationResult(false, List.of(DEFAULT_ERROR_MESSAGE));
        }
        return new ValidationResult(false, errorMessages);
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isInvalid() {
        return !valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    /**
     * Retorna el primer mensaje de error registrado, o una cadena vacía si el resultado es válido.
     */
    public String getFirstErrorMessage() {
        return errors.isEmpty() ? "" : errors.getFirst();
    }

    /**
     * Retorna todos los mensajes de error concatenados con salto de línea.
     */
    public String getCombinedErrorMessage() {
        return String.join("\n", errors);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult that = (ValidationResult) o;
        return valid == that.valid && Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, errors);
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", errors=" + errors +
                '}';
    }
}
