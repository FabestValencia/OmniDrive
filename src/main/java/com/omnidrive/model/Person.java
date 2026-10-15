package com.omnidrive.model;

import com.omnidrive.exception.InvalidUserDataException;
import com.omnidrive.model.enums.UserRole;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Clase base abstracta que representa a toda persona registrada en OmniDrive.
 * Proporciona validación defensiva de cédula, nombres, teléfono y correo electrónico.
 */
public abstract class Person implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final String id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;

    public Person(String id, String firstName, String lastName, String phone, String email) throws InvalidUserDataException {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidUserDataException("El número de cédula o identificación no puede estar vacío.");
        }
        this.id = id.trim();
        setFirstName(firstName);
        setLastName(lastName);
        setPhone(phone);
        setEmail(email);
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) throws InvalidUserDataException {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new InvalidUserDataException("Los nombres no pueden estar vacíos.");
        }
        this.firstName = firstName.trim();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) throws InvalidUserDataException {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new InvalidUserDataException("Los apellidos no pueden estar vacíos.");
        }
        this.lastName = lastName.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) throws InvalidUserDataException {
        if (phone == null || phone.trim().isEmpty()) {
            throw new InvalidUserDataException("El teléfono no puede estar vacío.");
        }
        this.phone = phone.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) throws InvalidUserDataException {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidUserDataException("El correo electrónico no puede estar vacío.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new InvalidUserDataException("El correo electrónico no tiene un formato válido: " + email);
        }
        this.email = normalizedEmail;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Retorna el rol específico del actor dentro del sistema.
     *
     * @return {@link UserRole} correspondiente.
     */
    public abstract UserRole getRole();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass() && !(o instanceof Person)) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id='" + id + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", role=" + getRole() +
                '}';
    }
}
