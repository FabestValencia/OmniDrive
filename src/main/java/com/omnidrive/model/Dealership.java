package com.omnidrive.model;

import com.omnidrive.exception.AuthenticationException;
import com.omnidrive.exception.BlockedUserException;
import com.omnidrive.exception.DuplicateTransactionException;
import com.omnidrive.exception.DuplicateUserException;
import com.omnidrive.exception.DuplicateVehicleException;
import com.omnidrive.exception.InvalidRecoveryCodeException;
import com.omnidrive.exception.InvalidTransactionException;
import com.omnidrive.exception.InvalidUserDataException;
import com.omnidrive.exception.InvalidVehicleDataException;
import com.omnidrive.exception.TechnicalReviewRejectedException;
import com.omnidrive.exception.VehicleUnavailableException;
import com.omnidrive.model.enums.VehicleCondition;
import com.omnidrive.model.enums.VehicleStatus;
import com.omnidrive.util.EmailService;
import com.omnidrive.util.MockEmailService;

import java.io.Serial;
import java.io.Serializable;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contenedor central del modelo en OmniDrive (Patrón MVC).
 * Centraliza la gestión del inventario de vehículos, actores (Clientes, Empleados, Administrador),
 * autenticación segura, recuperación de contraseñas y validaciones de negocio.
 */
public class Dealership implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private final Map<String, Vehicle> vehicles;
    private final Map<String, Customer> customers;
    private final Map<String, Employee> employees;
    private final Map<String, PasswordRecoveryToken> recoveryTokens;
    private final Map<String, Transaction> transactions;
    private int transactionSequence;
    private Administrator administrator;
    private EmailService emailService;

    public Dealership(String name) {
        if (name == null || name.trim().isEmpty()) {
            this.name = "OmniDrive Concesionario";
        } else {
            this.name = name.trim();
        }
        this.vehicles = new LinkedHashMap<>();
        this.customers = new LinkedHashMap<>();
        this.employees = new LinkedHashMap<>();
        this.recoveryTokens = new LinkedHashMap<>();
        this.transactions = new LinkedHashMap<>();
        this.transactionSequence = 0;
        this.emailService = new MockEmailService();

        // RF-20: Garantizar cuenta de Administrador raíz por defecto
        try {
            this.administrator = new Administrator(
                    "ADMIN-001", "Admin", "OmniDrive", "3000000000",
                    "admin@omnidrive.com", "admin", "Admin123*"
            );
        } catch (InvalidUserDataException e) {
            throw new IllegalStateException("Error crítico al inicializar el Administrador raíz por defecto", e);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService != null ? emailService : new MockEmailService();
    }

    // ==========================================
    // GESTIÓN DE VEHÍCULOS
    // ==========================================

    public void registerVehicle(Vehicle vehicle) throws InvalidVehicleDataException, DuplicateVehicleException, TechnicalReviewRejectedException {
        if (vehicle == null) {
            throw new InvalidVehicleDataException("El vehículo a registrar no puede ser nulo.");
        }

        String plate = vehicle.getPlate();
        if (vehicles.containsKey(plate)) {
            throw new DuplicateVehicleException(plate);
        }

        // RF-16: Estado inicial debe ser venta o alquiler
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE_FOR_SALE && vehicle.getStatus() != VehicleStatus.AVAILABLE_FOR_RENT) {
            throw new InvalidVehicleDataException("El estado inicial de disponibilidad debe ser 'Disponible para venta' o 'Disponible para alquiler'.");
        }

        // RF-14, RF-18: Revisión técnica aprobada obligatoria para usados
        if (vehicle.getCondition() == VehicleCondition.USED && !vehicle.isTechnicalReviewApproved()) {
            throw new TechnicalReviewRejectedException(plate);
        }

        vehicles.put(plate, vehicle);
    }

    public Vehicle findVehicleByPlate(String plate) {
        if (plate == null || plate.trim().isEmpty()) {
            return null;
        }
        return vehicles.get(plate.trim().toUpperCase());
    }

    public boolean containsVehicle(String plate) {
        if (plate == null || plate.trim().isEmpty()) {
            return false;
        }
        return vehicles.containsKey(plate.trim().toUpperCase());
    }

    public boolean removeVehicle(String plate) {
        if (plate == null || plate.trim().isEmpty()) {
            return false;
        }
        return vehicles.remove(plate.trim().toUpperCase()) != null;
    }

    public List<Vehicle> getAllVehicles() {
        return Collections.unmodifiableList(List.copyOf(vehicles.values()));
    }

    public List<Vehicle> filterVehicles(VehicleFilter filter) {
        if (filter == null) {
            return getAllVehicles();
        }
        return vehicles.values().stream()
                .filter(filter::matches)
                .toList();
    }

    public int getVehicleCount() {
        return vehicles.size();
    }

    // ==========================================
    // GESTIÓN DE ACTORES (CLIENTES Y EMPLEADOS)
    // ==========================================

    public void registerCustomer(Customer customer) throws DuplicateUserException, InvalidUserDataException {
        if (customer == null) {
            throw new InvalidUserDataException("El cliente a registrar no puede ser nulo.");
        }
        String id = customer.getId();
        validateIdUniqueness(id);
        customers.put(id, customer);
    }

    public Customer findCustomerById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return customers.get(id.trim());
    }

    public List<Customer> getAllCustomers() {
        return Collections.unmodifiableList(List.copyOf(customers.values()));
    }

    public int getCustomerCount() {
        return customers.size();
    }

    public void registerEmployee(Employee employee) throws DuplicateUserException, InvalidUserDataException {
        if (employee == null) {
            throw new InvalidUserDataException("El empleado a registrar no puede ser nulo.");
        }
        String id = employee.getId();
        validateIdUniqueness(id);

        String username = employee.getUsername();
        validateUsernameUniqueness(username);

        employees.put(id, employee);
    }

    public Employee findEmployeeById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return employees.get(id.trim());
    }

    public Employee findEmployeeByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        String normalizedUser = username.trim().toLowerCase();
        for (Employee emp : employees.values()) {
            if (emp.getUsername().equals(normalizedUser)) {
                return emp;
            }
        }
        return null;
    }

    public List<Employee> getAllEmployees() {
        return Collections.unmodifiableList(List.copyOf(employees.values()));
    }

    public int getEmployeeCount() {
        return employees.size();
    }

    public void updateEmployee(String id, String firstName, String lastName, String phone, String email) throws InvalidUserDataException {
        Employee employee = findEmployeeById(id);
        if (employee == null) {
            throw new InvalidUserDataException("No se encontró el empleado con cédula: " + id);
        }
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setPhone(phone);
        employee.setEmail(email);
    }

    public void blockEmployee(String id) throws InvalidUserDataException {
        Employee employee = findEmployeeById(id);
        if (employee == null) {
            throw new InvalidUserDataException("No se encontró el empleado con cédula: " + id);
        }
        employee.block();
    }

    public Administrator getAdministrator() {
        return administrator;
    }

    public void setAdministrator(Administrator administrator) throws InvalidUserDataException {
        if (administrator == null) {
            throw new InvalidUserDataException("El administrador no puede ser nulo.");
        }
        this.administrator = administrator;
    }

    // ==========================================
    // AUTENTICACIÓN Y RECUPERACIÓN DE CLAVES
    // ==========================================

    /**
     * Autentica a un usuario (Administrador o Empleado) verificando credenciales y estado (RF-08, RF-15, RF-16).
     *
     * @param username Nombre de usuario.
     * @param password Contraseña en texto plano.
     * @return El {@link User} autenticado.
     * @throws AuthenticationException Si las credenciales son incorrectas o faltan datos.
     * @throws BlockedUserException Si el usuario es un empleado en estado bloqueado.
     */
    public User authenticate(String username, String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Las credenciales de acceso no pueden estar vacías.");
        }

        String normalizedUser = username.trim().toLowerCase();
        User candidate = null;

        if (administrator != null && administrator.getUsername().equals(normalizedUser)) {
            candidate = administrator;
        } else {
            candidate = findEmployeeByUsername(normalizedUser);
        }

        if (candidate == null) {
            throw new AuthenticationException("Credenciales de acceso incorrectas.");
        }

        // RF-06, RF-16: Empleado bloqueado no puede iniciar sesión
        if (candidate instanceof Employee emp && emp.isBlocked()) {
            throw new BlockedUserException("La cuenta de empleado se encuentra bloqueada. Acceso denegado.");
        }

        if (!candidate.verifyPassword(password)) {
            throw new AuthenticationException("Credenciales de acceso incorrectas.");
        }

        return candidate;
    }

    /**
     * Solicita la recuperación de contraseña generando un código temporal de 15 minutos enviado al correo registrado (RF-13).
     *
     * @param usernameOrEmail Nombre de usuario o dirección de correo electrónico registrada.
     * @param now Instante temporal de la solicitud.
     * @return Código de recuperación generado.
     * @throws InvalidUserDataException Si el usuario no existe o los datos son inválidos.
     */
    public String requestPasswordRecovery(String usernameOrEmail, Instant now) throws InvalidUserDataException {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            throw new InvalidUserDataException("Debe ingresar un nombre de usuario o correo electrónico.");
        }

        User user = findUserByUsernameOrEmail(usernameOrEmail);
        if (user == null) {
            throw new InvalidUserDataException("No se encontró ningún usuario con el identificador o correo suministrado: " + usernameOrEmail);
        }

        // Generar código numérico seguro de 6 dígitos
        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        Instant requestInstant = now != null ? now : Instant.now();

        PasswordRecoveryToken token = new PasswordRecoveryToken(user.getUsername(), code, requestInstant);
        recoveryTokens.put(user.getUsername(), token);

        if (emailService != null) {
            emailService.sendEmail(
                    user.getEmail(),
                    "Recuperación de Contraseña - OmniDrive",
                    "Hola " + user.getFirstName() + ", tu código de recuperación es: " + code +
                            ". Es válido por 15 minutos."
            );
        }

        return code;
    }

    /**
     * Restablece la contraseña de un usuario mediante su código de verificación temporal (RF-14, RF-19).
     *
     * @param usernameOrEmail Nombre de usuario o correo.
     * @param code Código de verificación recibido.
     * @param newPassword Nueva contraseña a establecer.
     * @param now Instante temporal de la solicitud.
     * @throws AuthenticationException Si no se encuentra el usuario.
     * @throws InvalidUserDataException Si los datos son inválidos.
     * @throws InvalidRecoveryCodeException Si el código es erróneo, ha expirado o ya fue utilizado.
     */
    public void resetPasswordWithCode(String usernameOrEmail, String code, String newPassword, Instant now)
            throws AuthenticationException, InvalidUserDataException, InvalidRecoveryCodeException {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            throw new InvalidUserDataException("El usuario o correo no puede estar vacío.");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidRecoveryCodeException("El código de verificación no puede estar vacío.");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new InvalidUserDataException("La nueva contraseña no puede estar vacía.");
        }

        User user = findUserByUsernameOrEmail(usernameOrEmail);
        if (user == null) {
            throw new AuthenticationException("Usuario no encontrado.");
        }

        PasswordRecoveryToken token = recoveryTokens.get(user.getUsername());
        Instant verifyInstant = now != null ? now : Instant.now();

        if (token == null) {
            throw new InvalidRecoveryCodeException("No existe ninguna solicitud de recuperación activa para este usuario.");
        }

        if (verifyInstant.isAfter(token.getExpiresAt())) {
            throw new InvalidRecoveryCodeException("El código de recuperación ha expirado. Por favor solicite uno nuevo.");
        }

        if (!token.isValid(code, verifyInstant)) {
            throw new InvalidRecoveryCodeException("El código de recuperación es incorrecto o ya ha sido utilizado.");
        }

        user.changePassword(newPassword);
        token.markAsUsed();
    }

    public User findUserByUsernameOrEmail(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        String normalized = identifier.trim().toLowerCase();

        if (administrator != null) {
            if (administrator.getUsername().equals(normalized) || administrator.getEmail().equals(normalized)) {
                return administrator;
            }
        }

        for (Employee emp : employees.values()) {
            if (emp.getUsername().equals(normalized) || emp.getEmail().equals(normalized)) {
                return emp;
            }
        }

        return null;
    }

    private void validateIdUniqueness(String id) throws DuplicateUserException {
        if (customers.containsKey(id) || employees.containsKey(id) || (administrator != null && administrator.getId().equals(id))) {
            throw new DuplicateUserException(id, "El número de documento de identidad ya se encuentra registrado.");
        }
    }

    private void validateUsernameUniqueness(String username) throws DuplicateUserException {
        String normalized = username.trim().toLowerCase();
        if (administrator != null && administrator.getUsername().equals(normalized)) {
            throw new DuplicateUserException(username, "El nombre de usuario ya está reservado por el Administrador.");
        }
        for (Employee emp : employees.values()) {
            if (emp.getUsername().equals(normalized)) {
                throw new DuplicateUserException(username, "El nombre de usuario ya se encuentra registrado por otro empleado.");
            }
        }
    }

    // ==========================================
    // GESTIÓN DE TRANSACCIONES COMERCIALES
    // ==========================================

    /**
     * Genera un identificador único y secuencial para transacciones (formato TXN-%05d).
     */
    public synchronized String generateTransactionId() {
        return String.format("TXN-%05d", ++transactionSequence);
    }

    /**
     * Registra la compra de un vehículo a un cliente externo.
     * Exige empleado activo, validación de revisión técnica si es usado y placa no duplicada.
     */
    public PurchaseTransaction buyVehicle(Employee employee, Customer customer, Vehicle vehicle,
                                          double amount, VehicleStatus initialStatus, Instant timestamp)
            throws BlockedUserException, DuplicateVehicleException, TechnicalReviewRejectedException,
                   InvalidTransactionException, InvalidVehicleDataException {
        if (employee == null) {
            throw new InvalidTransactionException("El empleado responsable no puede ser nulo.");
        }
        if (employee.isBlocked()) {
            throw new BlockedUserException("El empleado " + employee.getUsername() + " está bloqueado y no puede registrar compras.");
        }
        if (customer == null) {
            throw new InvalidTransactionException("El cliente vendedor no puede ser nulo.");
        }
        if (vehicle == null) {
            throw new InvalidTransactionException("El vehículo objeto de compra no puede ser nulo.");
        }
        if (vehicles.containsKey(vehicle.getPlate())) {
            throw new DuplicateVehicleException(vehicle.getPlate());
        }
        if (initialStatus != VehicleStatus.AVAILABLE_FOR_SALE && initialStatus != VehicleStatus.AVAILABLE_FOR_RENT) {
            throw new InvalidTransactionException("El estado inicial tras la compra debe ser AVAILABLE_FOR_SALE o AVAILABLE_FOR_RENT.");
        }

        // Validación de revisión técnica si es usado
        if (vehicle.getCondition() == VehicleCondition.USED && !vehicle.isTechnicalReviewApproved()) {
            throw new TechnicalReviewRejectedException("No se puede comprar el vehículo usado " + vehicle.getPlate() + " sin revisión técnica aprobada.");
        }

        vehicle.setStatus(initialStatus);
        vehicles.put(vehicle.getPlate(), vehicle);

        String txnId = generateTransactionId();
        Instant txnTime = timestamp != null ? timestamp : Instant.now();
        PurchaseTransaction transaction = new PurchaseTransaction(txnId, employee, customer, vehicle, amount, txnTime, initialStatus);
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    /**
     * Registra la venta de un vehículo a un cliente.
     * Exige empleado activo, que el vehículo esté en AVAILABLE_FOR_SALE y conmuta su estado a SOLD.
     */
    public SaleTransaction sellVehicle(Employee employee, Customer customer, String plate,
                                       double amount, Instant timestamp)
            throws BlockedUserException, VehicleUnavailableException, InvalidTransactionException {
        if (employee == null) {
            throw new InvalidTransactionException("El empleado responsable no puede ser nulo.");
        }
        if (employee.isBlocked()) {
            throw new BlockedUserException("El empleado " + employee.getUsername() + " está bloqueado y no puede registrar ventas.");
        }
        if (customer == null) {
            throw new InvalidTransactionException("El cliente comprador no puede ser nulo.");
        }
        if (plate == null || plate.isBlank()) {
            throw new InvalidTransactionException("La placa del vehículo no puede ser nula ni vacía.");
        }

        Vehicle vehicle = findVehicleByPlate(plate);
        if (vehicle == null) {
            throw new VehicleUnavailableException("Vehículo no encontrado con placa: " + plate);
        }
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE_FOR_SALE) {
            throw new VehicleUnavailableException("El vehículo con placa " + plate + " no está disponible para venta. Estado actual: " + vehicle.getStatus());
        }

        String txnId = generateTransactionId();
        Instant txnTime = timestamp != null ? timestamp : Instant.now();
        SaleTransaction transaction = new SaleTransaction(txnId, employee, customer, vehicle, amount, txnTime);
        vehicle.setStatus(VehicleStatus.SOLD);
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    /**
     * Registra el alquiler de un vehículo a un cliente.
     * Exige empleado activo, que el vehículo esté en AVAILABLE_FOR_RENT, fechas consistentes,
     * calcula la tarifa total y conmuta el estado a RENTED.
     */
    public RentalTransaction rentVehicle(Employee employee, Customer customer, String plate,
                                         LocalDate startDate, LocalDate endDate, int rentalDays,
                                         Instant timestamp)
            throws BlockedUserException, VehicleUnavailableException, InvalidTransactionException {
        if (employee == null) {
            throw new InvalidTransactionException("El empleado responsable no puede ser nulo.");
        }
        if (employee.isBlocked()) {
            throw new BlockedUserException("El empleado " + employee.getUsername() + " está bloqueado y no puede registrar alquileres.");
        }
        if (customer == null) {
            throw new InvalidTransactionException("El cliente arrendatario no puede ser nulo.");
        }
        if (plate == null || plate.isBlank()) {
            throw new InvalidTransactionException("La placa del vehículo no puede ser nula ni vacía.");
        }

        Vehicle vehicle = findVehicleByPlate(plate);
        if (vehicle == null) {
            throw new VehicleUnavailableException("Vehículo no encontrado con placa: " + plate);
        }
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE_FOR_RENT) {
            throw new VehicleUnavailableException("El vehículo con placa " + plate + " no está disponible para alquiler. Estado actual: " + vehicle.getStatus());
        }

        String txnId = generateTransactionId();
        Instant txnTime = timestamp != null ? timestamp : Instant.now();
        RentalTransaction transaction = new RentalTransaction(txnId, employee, customer, vehicle, txnTime, startDate, endDate, rentalDays);
        vehicle.setStatus(VehicleStatus.RENTED);
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    /**
     * Registra la devolución de un vehículo alquilado, retornando su estado a AVAILABLE_FOR_RENT.
     * Exige que el vehículo se encuentre actualmente en estado RENTED.
     */
    public Vehicle returnRentedVehicle(String plate)
            throws VehicleUnavailableException, InvalidTransactionException {
        if (plate == null || plate.isBlank()) {
            throw new InvalidTransactionException("La placa del vehículo devuelto no puede ser nula ni vacía.");
        }

        Vehicle vehicle = findVehicleByPlate(plate);
        if (vehicle == null) {
            throw new VehicleUnavailableException("Vehículo no encontrado con placa: " + plate);
        }
        if (vehicle.getStatus() != VehicleStatus.RENTED) {
            throw new VehicleUnavailableException("El vehículo con placa " + plate + " no se encuentra en arriendo. Estado actual: " + vehicle.getStatus());
        }

        vehicle.setStatus(VehicleStatus.AVAILABLE_FOR_RENT);
        return vehicle;
    }

    public Transaction findTransactionById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return transactions.get(id.trim());
    }

    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(new ArrayList<>(transactions.values()));
    }

    public List<Transaction> getTransactionsByEmployee(String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            return Collections.emptyList();
        }
        List<Transaction> result = new ArrayList<>();
        for (Transaction txn : transactions.values()) {
            if (txn.getEmployee().getId().equals(employeeId.trim())) {
                result.add(txn);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public void registerTransaction(Transaction transaction)
            throws DuplicateTransactionException, InvalidTransactionException {
        if (transaction == null) {
            throw new InvalidTransactionException("La transacción a registrar no puede ser nula.");
        }
        if (transactions.containsKey(transaction.getId())) {
            throw new DuplicateTransactionException("Transacción duplicada: " + transaction.getId());
        }
        transactions.put(transaction.getId(), transaction);
    }
}
