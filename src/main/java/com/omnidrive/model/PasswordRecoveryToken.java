package com.omnidrive.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Representa un token de verificación temporal para la recuperación de contraseñas de usuarios.
 * La validez es evaluada mediante inyección temporal explícita para garantizar pruebas deterministas.
 */
public class PasswordRecoveryToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Duration DEFAULT_EXPIRATION = Duration.ofMinutes(15);

    private final String username;
    private final String code;
    private final Instant createdAt;
    private final Instant expiresAt;
    private boolean isUsed;

    public PasswordRecoveryToken(String username, String code, Instant createdAt) {
        this(username, code, createdAt, DEFAULT_EXPIRATION);
    }

    public PasswordRecoveryToken(String username, String code, Instant createdAt, Duration validityDuration) {
        this.username = Objects.requireNonNull(username, "El nombre de usuario no puede ser nulo.").trim().toLowerCase();
        this.code = Objects.requireNonNull(code, "El código de verificación no puede ser nulo.").trim();
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación no puede ser nula.");
        Duration duration = validityDuration != null ? validityDuration : DEFAULT_EXPIRATION;
        this.expiresAt = this.createdAt.plus(duration);
        this.isUsed = false;
    }

    public String getUsername() {
        return username;
    }

    public String getCode() {
        return code;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void markAsUsed() {
        this.isUsed = true;
    }

    /**
     * Evalúa si el token es válido para un código suministrado y en un instante de tiempo dado.
     *
     * @param candidateCode Código ingresado por el usuario.
     * @param now Instante temporal en el que se intenta la validación.
     * @return {@code true} si no ha sido usado, el código coincide exactamente y no ha expirado; {@code false} en caso contrario.
     */
    public boolean isValid(String candidateCode, Instant now) {
        if (isUsed) {
            return false;
        }
        if (candidateCode == null || !this.code.equals(candidateCode.trim())) {
            return false;
        }
        if (now == null || now.isAfter(expiresAt)) {
            return false;
        }
        return true;
    }
}
