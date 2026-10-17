package com.omnidrive.util;

import com.omnidrive.exception.PersistenceException;
import com.omnidrive.model.Dealership;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Guardado de Concesionario (T2)")
class DealershipSaveTest {

    @Test
    @DisplayName("Debe persistir exitosamente el estado de Dealership en un archivo binario")
    void testSaveDealershipSuccessfully(@TempDir Path tempDir) throws Exception {
        Dealership dealership = new Dealership("OmniDrive");
        Path targetFile = tempDir.resolve("dealership.dat");

        DealershipPersistenceManager.save(dealership, targetFile);

        assertTrue(Files.exists(targetFile), "El archivo de persistencia debe existir tras el guardado");
        assertTrue(Files.size(targetFile) > 0, "El archivo persistido no debe estar vacío");
    }

    @Test
    @DisplayName("Debe crear automáticamente los directorios padre si no existen")
    void testSaveCreatesParentDirectoriesAutomatically(@TempDir Path tempDir) throws Exception {
        Dealership dealership = new Dealership("OmniDrive");
        Path nestedDir = tempDir.resolve("deep").resolve("nested").resolve("storage");
        Path targetFile = nestedDir.resolve("data.bin");

        assertFalse(Files.exists(nestedDir), "El directorio padre no debe existir previamente");

        DealershipPersistenceManager.save(dealership, targetFile);

        assertTrue(Files.exists(nestedDir), "El directorio padre debió ser creado automáticamente");
        assertTrue(Files.exists(targetFile), "El archivo persistido debe existir dentro de la ruta anidada");
        assertTrue(Files.size(targetFile) > 0, "El archivo persistido debe contener bytes de datos");
    }

    @Test
    @DisplayName("Debe arrojar PersistenceException si la instancia de Dealership es nula (RF-10)")
    void testSaveRejectsNullDealership(@TempDir Path tempDir) {
        Path targetFile = tempDir.resolve("dealership.dat");

        PersistenceException ex = assertThrows(PersistenceException.class, () ->
                DealershipPersistenceManager.save(null, targetFile)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("null") || ex.getMessage().toLowerCase().contains("nulo"));
    }

    @Test
    @DisplayName("Debe arrojar PersistenceException si la ruta Path es nula (RF-09)")
    void testSaveRejectsNullFilePath() {
        Dealership dealership = new Dealership("OmniDrive");

        PersistenceException ex = assertThrows(PersistenceException.class, () ->
                DealershipPersistenceManager.save(dealership, null)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("null") || ex.getMessage().toLowerCase().contains("nula"));
    }

    @Test
    @DisplayName("Debe arrojar PersistenceException ante ruta inaccesible o de tipo directorio (RF-09)")
    void testSaveRejectsInaccessiblePath(@TempDir Path tempDir) throws IOException {
        Dealership dealership = new Dealership("OmniDrive");
        Path existingDirectory = tempDir.resolve("forbidden_directory");
        Files.createDirectory(existingDirectory);

        // Intentar escribir un archivo con el nombre exacto de un directorio existente provoca fallo de E/S
        PersistenceException ex = assertThrows(PersistenceException.class, () ->
                DealershipPersistenceManager.save(dealership, existingDirectory)
        );

        assertNotNull(ex.getMessage());
        assertNotNull(ex.getCause());
    }
}
