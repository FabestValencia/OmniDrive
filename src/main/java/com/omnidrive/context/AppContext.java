package com.omnidrive.context;

import com.omnidrive.exception.PersistenceException;
import com.omnidrive.model.Administrator;
import com.omnidrive.model.Dealership;
import com.omnidrive.model.Employee;
import com.omnidrive.model.User;
import com.omnidrive.util.DealershipPersistenceManager;

import java.nio.file.Path;

/**
 * Contexto global de ejecución para la interfaz gráfica OmniDrive (Patrón MVC).
 * Centraliza la sesión del usuario autenticado, la referencia al {@link Dealership}
 * activo y las sincronizaciones automáticas con el archivo de persistencia local.
 */
public final class AppContext {

    private static Dealership dealership;
    private static User currentUser;
    private static Path storagePath;

    private AppContext() {
        // Constructor privado para clase estática de contexto
    }

    /**
     * Inicializa el contexto cargando o creando por defecto el concesionario desde la ruta local.
     *
     * @param path Ruta del archivo local de persistencia (no nula).
     */
    public static synchronized void init(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("La ruta de persistencia no puede ser nula.");
        }
        storagePath = path;
        try {
            dealership = DealershipPersistenceManager.loadOrCreateDefault(path);
        } catch (PersistenceException e) {
            throw new IllegalStateException("Error al inicializar el concesionario desde: " + path, e);
        }
    }

    public static Dealership getDealership() {
        return dealership;
    }

    public static Path getStoragePath() {
        return storagePath;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isAuthenticated() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser instanceof Administrator;
    }

    public static boolean isEmployee() {
        return currentUser instanceof Employee;
    }

    public static synchronized void login(User user) {
        currentUser = user;
    }

    public static synchronized void logout() {
        currentUser = null;
    }

    /**
     * Persiste atómicamente el estado actual del concesionario en la ruta configurada.
     *
     * @throws PersistenceException Si ocurre un fallo en la serialización o escritura.
     */
    public static synchronized void save() throws PersistenceException {
        if (dealership != null && storagePath != null) {
            DealershipPersistenceManager.save(dealership, storagePath);
        }
    }

    /**
     * Reinicia el estado del contexto (utilizado principalmente para aislamiento de pruebas).
     */
    public static synchronized void reset() {
        dealership = null;
        currentUser = null;
        storagePath = null;
    }
}
