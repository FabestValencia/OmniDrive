package com.omnidrive.model;

import com.omnidrive.exception.InvalidUserDataException;
import com.omnidrive.model.enums.UserRole;

import java.io.Serial;

/**
 * Representa a un cliente del concesionario OmniDrive.
 * Los clientes no poseen credenciales de acceso al sistema ni nombre de usuario.
 */
public class Customer extends Person {

    @Serial
    private static final long serialVersionUID = 1L;

    public Customer(String id, String firstName, String lastName, String phone, String email) throws InvalidUserDataException {
        super(id, firstName, lastName, phone, email);
    }

    @Override
    public UserRole getRole() {
        return UserRole.CUSTOMER;
    }
}
