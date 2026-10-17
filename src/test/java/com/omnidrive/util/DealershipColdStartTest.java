package com.omnidrive.util;

import com.omnidrive.exception.PersistenceException;
import com.omnidrive.model.Dealership;
import com.omnidrive.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Arranque en Frío (loadOrCreateDefault) (T4)")
class DealershipColdStartTest {

    @Test
    @DisplayName("Debe inicializar un nuevo concesionario con Administrador raíz si el archivo no existe (RF-04, Escenario 1)")
    void testColdStartCreatesNewDealershipWhenFileDoesNotExist(@TempDir Path tempDir) throws Exception {
        Path missingFile = tempDir.resolve("non_existent_dealership.dat");
        assertFalse(Files.exists(missingFile), "El archivo no debe existir inicialmente");

        Dealership dealership = DealershipPersistenceManager.loadOrCreateDefault(missingFile);

        assertNotNull(dealership, "La instancia creada en arranque en frío no debe ser nula");
        assertNotNull(dealership.getAdministrator(), "Debe contar con el Administrador raíz por defecto");
        assertEquals("ADMIN-001", dealership.getAdministrator().getId());
        assertEquals("admin", dealership.getAdministrator().getUsername());

        // Debe permitir autenticación inmediata con las credenciales por defecto
        User admin = dealership.authenticate("admin", "Admin123*");
        assertNotNull(admin);
        assertEquals("ADMIN-001", admin.getId());
    }

    @Test
    @DisplayName("Debe cargar el concesionario existente si el archivo ya existe (RF-04, RF-07)")
    void testColdStartLoadsExistingDealershipWhenFileExists(@TempDir Path tempDir) throws Exception {
        Path targetFile = tempDir.resolve("existing_dealership.dat");
        Dealership existing = new Dealership("OmniDrive Sucursal Norte");
        DealershipPersistenceManager.save(existing, targetFile);
        assertTrue(Files.exists(targetFile));

        Dealership loaded = DealershipPersistenceManager.loadOrCreateDefault(targetFile);

        assertNotNull(loaded);
        assertEquals("OmniDrive Sucursal Norte", loaded.getName());
    }

    @Test
    @DisplayName("Debe arrojar PersistenceException si el archivo existe pero está corrupto (RF-08)")
    void testColdStartRejectsCorruptedFileWithoutOverwriting(@TempDir Path tempDir) throws IOException {
        Path corruptedFile = tempDir.resolve("corrupted_dealership.dat");
        Files.write(corruptedFile, new byte[]{0x00, 0x01, 0x02, (byte) 0xFE, (byte) 0xFF});

        assertThrows(PersistenceException.class, () ->
                DealershipPersistenceManager.loadOrCreateDefault(corruptedFile)
        );
    }

    @Test
    @DisplayName("Debe arrojar PersistenceException si la ruta Path es nula (RF-09)")
    void testColdStartRejectsNullFilePath() {
        assertThrows(PersistenceException.class, () ->
                DealershipPersistenceManager.loadOrCreateDefault(null)
        );
    }
}
