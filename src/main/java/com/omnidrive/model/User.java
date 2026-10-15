package com.omnidrive.model;

import com.omnidrive.exception.InvalidUserDataException;

import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Clase base abstracta para personas con credenciales de acceso a la plataforma (Empleados y Administradores).
 * Garantiza que las contraseñas nunca se almacenen en texto plano utilizando hashing SHA-256.
 */
public abstract class User extends Person {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private String passwordHash;

    public User(String id, String firstName, String lastName, String phone, String email,
                String username, String plainPassword) throws InvalidUserDataException {
        super(id, firstName, lastName, phone, email);

        if (username == null || username.trim().isEmpty()) {
            throw new InvalidUserDataException("El nombre de usuario no puede estar vacío.");
        }
        this.username = username.trim().toLowerCase();
        changePassword(plainPassword);
    }

    public String getUsername() {
        return username;
    }

    /**
     * Valida y actualiza la contraseña del usuario calculando su hash seguro.
     *
     * @param newPlainPassword Nueva contraseña en texto plano.
     * @throws InvalidUserDataException Si la contraseña es nula, vacía o contiene solo espacios.
     */
    public void changePassword(String newPlainPassword) throws InvalidUserDataException {
        if (newPlainPassword == null || newPlainPassword.trim().isEmpty()) {
            throw new InvalidUserDataException("La contraseña no puede estar vacía.");
        }
        this.passwordHash = hashPassword(newPlainPassword.trim());
    }

    /**
     * Verifica si la contraseña candidata coincide con el hash almacenado.
     *
     * @param candidatePassword Contraseña ingresada por el usuario.
     * @return {@code true} si la contraseña es correcta; {@code false} en caso contrario.
     */
    public boolean verifyPassword(String candidatePassword) {
        if (candidatePassword == null || candidatePassword.trim().isEmpty()) {
            return false;
        }
        String candidateHash = hashPassword(candidatePassword.trim());
        return MessageDigest.isEqual(
                candidateHash.getBytes(StandardCharsets.UTF_8),
                passwordHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Calcula el hash SHA-256 de una contraseña en texto plano.
     */
    private static String hashPassword(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error al inicializar el algoritmo de hash seguro SHA-256", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + getId() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", username='" + username + '\'' +
                ", role=" + getRole() +
                '}';
    }
}
