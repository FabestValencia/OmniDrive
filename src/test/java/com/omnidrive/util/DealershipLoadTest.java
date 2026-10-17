package com.omnidrive.util;

import com.omnidrive.exception.PersistenceException;
import com.omnidrive.model.Dealership;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Carga de Concesionario y Control de Corrupción (T3)")
class DealershipLoadTest {

    @Test
    @DisplayName("Debe deserializar y restaurar fielmente una instancia de Dealership (RF-02, RF-07)")
    void testLoadSuccessfullyRestoresDealership(@TempDir Path tempDir) throws Exception {
        Dealership original = new Dealership("OmniDrive Sucursal Central");
        Path targetFile = tempDir.resolve("dealership.dat");

        DealershipPersistenceManager.save(original, targetFile);

        Dealership loaded = DealershipPersistenceManager.load(targetFile);

        assertNotNull(loaded, "La instancia cargada no debe ser nula");
        assertEquals("OmniDrive Sucursal Central", loaded.getName());
        assertNotNull(loaded.getAdministrator(), "Debe conservar la cuenta del Administrador raíz");
        assertEquals("ADMIN-001", loaded.getAdministrator().getId());
    }

    @Test
    @DisplayName("Debe rechazar archivos con bytes corruptos o cabecera inválida arrojando PersistenceException (RF-08)")
    void testLoadRejectsCorruptedFileWithRandomBytes(@TempDir Path tempDir) throws IOException {
        Path corruptedFile = tempDir.resolve("corrupted.dat");
        byte[] garbage = new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC};
        Files.write(corruptedFile, garbage);

        PersistenceException ex = assertThrows(PersistenceException.class, () ->
                DealershipPersistenceManager.load(corruptedFile)
        );

        assertNotNull(ex.getMessage(), "El mensaje de error no debe ser nulo");
        assertNotNull(ex.getCause(), "Debe contener la excepción raíz de E/S o deserialización");
    }

    @Test
    @DisplayName("Debe rechazar archivos con texto plano o formato no serializable (RF-08)")
    void testLoadRejectsInvalidPlainTextFile(@TempDir Path tempDir) throws IOException {
        Path textFile = tempDir.resolve("invalid.txt");
        Files.writeString(textFile, "ESTO NO ES UN ARCHIVO SERIALIZADO DE JAVA", StandardCharsets.UTF_8);

        PersistenceException ex = assertThrows(PersistenceException.class, () ->
                DealershipPersistenceManager.load(textFile)
        );

        assertNotNull(ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    @DisplayName("Debe arrojar PersistenceException si el archivo a cargar no existe (RF-08, RF-09)")
    void testLoadRejectsNonExistentFile(@TempDir Path tempDir) {
        Path missingFile = tempDir.resolve("does_not_exist.dat");

        PersistenceException ex = assertThrows(PersistenceException.class, () ->
                DealershipPersistenceManager.load(missingFile)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("no existe") ||
                   ex.getMessage().toLowerCase().contains("not found") ||
                   ex.getMessage().toLowerCase().contains("error"));
    }

    @Test
    @DisplayName("Debe arrojar PersistenceException si la ruta Path es nula (RF-09)")
    void testLoadRejectsNullFilePath() {
        PersistenceException ex = assertThrows(PersistenceException.class, () ->
                DealershipPersistenceManager.load(null)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("null") || ex.getMessage().toLowerCase().contains("nula"));
    }
}
