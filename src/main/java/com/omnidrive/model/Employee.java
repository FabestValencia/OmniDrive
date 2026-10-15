package com.omnidrive.model;

import com.omnidrive.exception.InvalidUserDataException;
import com.omnidrive.model.enums.EmployeeStatus;
import com.omnidrive.model.enums.UserRole;

import java.io.Serial;

/**
 * Representa a un empleado operativo del concesionario OmniDrive.
 * Toda cuenta de empleado inicia en estado activo y puede ser bloqueada permanentemente.
 */
public class Employee extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    private EmployeeStatus status;

    public Employee(String id, String firstName, String lastName, String phone, String email,
                    String username, String plainPassword) throws InvalidUserDataException {
        super(id, firstName, lastName, phone, email, username, plainPassword);
        this.status = EmployeeStatus.ACTIVE; // RF-05: Toda cuenta inicia activa
    }

    @Override
    public UserRole getRole() {
        return UserRole.EMPLOYEE;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    /**
     * Indica si el empleado se encuentra bloqueado para operar en el sistema.
     *
     * @return {@code true} si el estado es {@link EmployeeStatus#BLOCKED}; {@code false} si está activo.
     */
    public boolean isBlocked() {
        return status == EmployeeStatus.BLOCKED;
    }

    /**
     * Bloquea permanentemente la cuenta del empleado (RF-11).
     * Esta acción es irreversible según las reglas de negocio del concesionario.
     */
    public void block() {
        this.status = EmployeeStatus.BLOCKED;
    }
}
