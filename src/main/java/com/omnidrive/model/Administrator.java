package com.omnidrive.model;

import com.omnidrive.exception.InvalidUserDataException;
import com.omnidrive.model.enums.UserRole;

import java.io.Serial;

/**
 * Representa al administrador del sistema OmniDrive.
 * Posee privilegios de gestión de personal, auditoría y supervisión global del concesionario.
 */
public class Administrator extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    public Administrator(String id, String firstName, String lastName, String phone, String email,
                         String username, String plainPassword) throws InvalidUserDataException {
        super(id, firstName, lastName, phone, email, username, plainPassword);
    }

    @Override
    public UserRole getRole() {
        return UserRole.ADMINISTRATOR;
    }
}
