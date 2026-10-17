package com.omnidrive.util;

import com.omnidrive.exception.PersistenceException;
import com.omnidrive.model.Dealership;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Gestor centralizado de persistencia para OmniDrive (Arquitectura MVC).
 * Encapsula las operaciones de guardado, carga y recuperación del estado
 * completo del modelo en almacenamiento local mediante serialización binaria.
 */
public final class DealershipPersistenceManager {

    private DealershipPersistenceManager() {
        // Constructor privado para clase utilitaria
    }

    /**
     * Serializa y escribe el estado completo de un {@link Dealership} en la ruta de archivo especificada.
     * Crea automáticamente los directorios padre si no existen previamente.
     *
     * @param dealership Instancia del concesionario a persistir (no puede ser nula).
     * @param filePath   Ruta del archivo local de destino (no puede ser nula).
     * @throws PersistenceException Si la instancia o la ruta son nulas, o si ocurre un fallo de E/S.
     */
    public static void save(Dealership dealership, Path filePath) throws PersistenceException {
        if (dealership == null) {
            throw new PersistenceException("No se puede persistir un estado nulo de Dealership (instancia nula).");
        }
        if (filePath == null) {
            throw new PersistenceException("La ruta del archivo de persistencia no puede ser nula.");
        }

        try {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filePath))) {
                oos.writeObject(dealership);
                oos.flush();
            }
        } catch (IOException e) {
            throw new PersistenceException("Error de E/S al guardar el concesionario en la ruta: " + filePath, e);
        }
    }

    /**
     * Lee y deserializa el estado de un {@link Dealership} desde la ruta de archivo especificada.
     * Detecta y rechaza archivos inexistentes, corruptos o no compatibles arrojando {@link PersistenceException}.
     *
     * @param filePath Ruta del archivo local a cargar (no puede ser nula).
     * @return La instancia restaurada de {@link Dealership}.
     * @throws PersistenceException Si la ruta es nula, el archivo no existe, está corrupto o no es válido.
     */
    public static Dealership load(Path filePath) throws PersistenceException {
        if (filePath == null) {
            throw new PersistenceException("La ruta del archivo de persistencia no puede ser nula.");
        }
        if (!Files.exists(filePath)) {
            throw new PersistenceException("El archivo de persistencia no existe en la ruta: " + filePath);
        }
        if (Files.isDirectory(filePath)) {
            throw new PersistenceException("La ruta de persistencia no puede ser un directorio: " + filePath);
        }

        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(Files.newInputStream(filePath))) {
            Object obj = ois.readObject();
            if (!(obj instanceof Dealership dealership)) {
                throw new PersistenceException("El contenido del archivo no corresponde a una instancia válida de Dealership: " + filePath);
            }
            return dealership;
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenceException("Error al leer o deserializar el archivo de persistencia (archivo corrupto o ilegible): " + filePath, e);
        }
    }

    /**
     * Carga el estado del concesionario si el archivo existe en la ruta dada; si el archivo no existe,
     * inicializa de forma transparente una nueva instancia de {@link Dealership} que garantiza la
     * cuenta del Administrador raíz (ADMIN-001) para el primer arranque del sistema (RF-04).
     * Si el archivo existe pero contiene datos corruptos, arroja {@link PersistenceException} sin sobreescribir.
     *
     * @param filePath Ruta del archivo local de persistencia (no puede ser nula).
     * @return La instancia de {@link Dealership} cargada o inicializada por defecto.
     * @throws PersistenceException Si la ruta es nula o si el archivo existe pero no se puede deserializar.
     */
    public static Dealership loadOrCreateDefault(Path filePath) throws PersistenceException {
        if (filePath == null) {
            throw new PersistenceException("La ruta del archivo de persistencia no puede ser nula.");
        }

        if (!Files.exists(filePath)) {
            return new Dealership();
        }

        return load(filePath);
    }
}
