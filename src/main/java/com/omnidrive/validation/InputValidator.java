package com.omnidrive.validation;

import java.time.LocalDate;
import java.time.Year;
import java.util.regex.Pattern;

/**
 * Motor centralizado y desacoplado de validación de entradas de datos para OmniDrive.
 * Proporciona comprobaciones estandarizadas para formatos vehiculares, datos personales,
 * conversiones numéricas seguras sin excepciones y rangos cronológicos.
 */
public final class InputValidator {

    private static final Pattern PLATE_CAR_PATTERN = Pattern.compile("^[A-Za-z]{3}-?[0-9]{3}$");
    private static final Pattern PLATE_MOTO_PATTERN = Pattern.compile("^[A-Za-z]{3}-?[0-9]{2}[A-Za-z]$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_DIGITS_PATTERN = Pattern.compile("^[0-9]{7,12}$");

    private InputValidator() {
        // Constructor privado para clase puramente utilitaria
    }

    /**
     * Valida el formato de una placa vehicular colombiana (Automóvil AAA-123 o Moto AAA-12A).
     */
    public static ValidationResult validatePlate(String plate) {
        if (plate == null || plate.trim().isEmpty()) {
            return ValidationResult.failure("La placa vehicular no puede estar vacía.");
        }
        String normalized = plate.trim();
        if (PLATE_CAR_PATTERN.matcher(normalized).matches() || PLATE_MOTO_PATTERN.matcher(normalized).matches()) {
            return ValidationResult.success();
        }
        return ValidationResult.failure(
                "Formato de placa inválido (" + plate + "). Debe contener 3 letras y 3 números (ej: ABC-123) o para moto (ej: ABC-12D).");
    }

    /**
     * Valida la identificación o cédula.
     */
    public static ValidationResult validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return ValidationResult.failure("El número de identificación no puede estar vacío.");
        }
        String normalized = id.trim();
        if (normalized.length() < 4 || normalized.length() > 15) {
            return ValidationResult.failure("La identificación debe tener entre 4 y 15 caracteres.");
        }
        return ValidationResult.success();
    }

    /**
     * Valida nombres o apellidos.
     */
    public static ValidationResult validateName(String name, String fieldName) {
        String label = (fieldName != null && !fieldName.isBlank()) ? fieldName : "El campo";
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.failure("El campo " + label + " no puede estar vacío.");
        }
        if (name.trim().length() < 2) {
            return ValidationResult.failure("El campo " + label + " debe contener al menos 2 caracteres.");
        }
        return ValidationResult.success();
    }

    /**
     * Valida el formato de correo electrónico bajo estándar RFC simplificado.
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.failure("El correo electrónico no puede estar vacío.");
        }
        String normalized = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            return ValidationResult.failure("El correo electrónico no tiene un formato válido (ej: usuario@dominio.com).");
        }
        return ValidationResult.success();
    }

    /**
     * Valida el teléfono eliminando espacios y guiones, exigiendo entre 7 y 12 dígitos.
     */
    public static ValidationResult validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return ValidationResult.failure("El número de teléfono no puede estar vacío.");
        }
        String digits = phone.replaceAll("[\\s-]", "");
        if (!PHONE_DIGITS_PATTERN.matcher(digits).matches()) {
            return ValidationResult.failure("El teléfono debe contener entre 7 y 12 dígitos numéricos.");
        }
        return ValidationResult.success();
    }

    /**
     * Valida el año de fabricación del vehículo (entre 1900 y año actual + 1).
     */
    public static ValidationResult validateModelYear(int year) {
        int maxAllowedYear = Year.now().getValue() + 1;
        if (year < 1900 || year > maxAllowedYear) {
            return ValidationResult.failure(
                    "El año de modelo (" + year + ") debe estar entre 1900 y " + maxAllowedYear + ".");
        }
        return ValidationResult.success();
    }

    /**
     * Parsea de forma segura un número entero que debe ser estrictamente mayor a 0.
     */
    public static ValidationResult parsePositiveInt(String input, String fieldName) {
        String label = (fieldName != null && !fieldName.isBlank()) ? fieldName : "El valor";
        if (input == null || input.trim().isEmpty()) {
            return ValidationResult.failure("El campo " + label + " no puede estar vacío.");
        }
        try {
            int val = Integer.parseInt(input.trim());
            if (val <= 0) {
                return ValidationResult.failure("El campo " + label + " debe ser un número entero mayor a 0.");
            }
            return ValidationResult.success();
        } catch (NumberFormatException e) {
            return ValidationResult.failure("El campo " + label + " debe ser un número entero válido.");
        }
    }

    /**
     * Parsea de forma segura un número decimal que debe ser estrictamente mayor a 0.
     * Soporta tanto punto como coma como separador decimal.
     */
    public static ValidationResult parsePositiveDouble(String input, String fieldName) {
        String label = (fieldName != null && !fieldName.isBlank()) ? fieldName : "El valor";
        if (input == null || input.trim().isEmpty()) {
            return ValidationResult.failure("El campo " + label + " no puede estar vacío.");
        }
        try {
            String normalized = input.trim().replace(",", ".");
            double val = Double.parseDouble(normalized);
            if (val <= 0.0) {
                return ValidationResult.failure("El campo " + label + " debe ser un valor numérico mayor a 0.");
            }
            return ValidationResult.success();
        } catch (NumberFormatException e) {
            return ValidationResult.failure("El campo " + label + " debe ser un número decimal válido.");
        }
    }

    /**
     * Valida un rango de fechas asegurando no nulidad y que la fecha inicial no sea posterior a la final.
     */
    public static ValidationResult validateDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return ValidationResult.failure("Las fechas de inicio y fin son obligatorias.");
        }
        if (start.isAfter(end)) {
            return ValidationResult.failure(
                    "La fecha de inicio (" + start + ") no puede ser posterior a la fecha de fin (" + end + ").");
        }
        return ValidationResult.success();
    }
}
