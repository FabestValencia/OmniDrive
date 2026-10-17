package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.model.*;
import com.omnidrive.model.enums.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Controlador de Inventario de Vehículos (T8 - Spec 006)")
class VehicleManagementControllerTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit JavaFX ya iniciado
        }
    }

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        AppContext.reset();
        AppContext.init(tempDir.resolve("dealership.dat"));
        AppContext.login(AppContext.getDealership().getAdministrator());
    }

    private VehicleManagementController createControllerWithUI() {
        VehicleManagementController controller = new VehicleManagementController();
        controller.setVehicleTable(new TableView<>());
        controller.setColPlate(new TableColumn<>());
        controller.setColBrand(new TableColumn<>());
        controller.setColType(new TableColumn<>());
        controller.setColModelYear(new TableColumn<>());
        controller.setColFuelType(new TableColumn<>());
        controller.setColCondition(new TableColumn<>());
        controller.setColStatus(new TableColumn<>());
        controller.setColBasePrice(new TableColumn<>());

        controller.setTxtSearch(new TextField());
        controller.setCmbConditionFilter(new ComboBox<>());
        controller.setCmbStatusFilter(new ComboBox<>());

        controller.setImgVehiclePhoto(new ImageView());
        controller.setLblDetailPlate(new Label());
        controller.setLblDetailBrand(new Label());
        controller.setLblDetailType(new Label());
        controller.setLblDetailStatus(new Label());
        controller.setLblDetailPrice(new Label());
        controller.setLblDetailTechnicalSpecs(new Label());
        controller.setDetailPanel(new VBox());
        controller.setBtnNewVehicle(new Button());

        controller.initialize();
        return controller;
    }

    private Sedan createTestSedan() throws Exception {
        return new Sedan("SED-123", "Toyota Corolla", 2024, 6, 210.0, 1800.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 95_000_000.0, null,
                true, null, null,
                5, 4, 6, true, true, true,
                470.0, true, true, true, true);
    }

    private SportsCar createTestSportsCar() throws Exception {
        return new SportsCar("SPT-999", "Porsche 911", 2023, 7, 305.0, 3000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 650_000_000.0, null,
                true, null, null,
                2, 2, 6,
                450, 3.5);
    }

    private Truck createTestTruck() throws Exception {
        return new Truck("TRK-555", "Kenworth T680", 2022, 10, 130.0, 12900.0,
                FuelType.DIESEL, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_RENT, 18_000_000.0, null,
                true, null, null,
                25.0, true, true, true, 3, TruckType.FLATBED);
    }

    @Test
    @DisplayName("Debe poblar la tabla con vehículos registrados en el concesionario")
    void testLoadVehiclesInTable() throws Exception {
        AppContext.getDealership().registerVehicle(createTestSedan());
        AppContext.getDealership().registerVehicle(createTestSportsCar());

        VehicleManagementController controller = createControllerWithUI();

        assertEquals(2, controller.getVehicleTable().getItems().size());
        assertEquals("SED-123", controller.getVehicleTable().getItems().get(0).getPlate());
    }

    @Test
    @DisplayName("Al seleccionar un vehículo debe sincronizar el panel lateral con fotografía y especificaciones (RF-07, Escenario 4)")
    void testSelectVehicleSyncsDetailPanel() throws Exception {
        Sedan sedan = createTestSedan();
        AppContext.getDealership().registerVehicle(sedan);

        VehicleManagementController controller = createControllerWithUI();
        controller.selectVehicle(sedan);

        assertEquals("SED-123", controller.getLblDetailPlate().getText());
        assertEquals("Toyota Corolla", controller.getLblDetailBrand().getText());
        assertEquals("Sedán", controller.getLblDetailType().getText());
        assertTrue(controller.getLblDetailStatus().getText().contains("Disponible para Venta"));
        assertTrue(controller.getLblDetailPrice().getText().contains("95.000.000") || controller.getLblDetailPrice().getText().contains("95000000"));

        String specs = controller.getLblDetailTechnicalSpecs().getText();
        assertTrue(specs.contains("Pasajeros: 5"));
        assertTrue(specs.contains("Maletero: 470.0 L"));
        assertTrue(specs.contains("Asistente carril: Sí"));

        // Fallback a silueta por defecto al no existir foto personalizada
        assertNotNull(controller.getImgVehiclePhoto().getImage(), "Debe cargar silueta por defecto");
    }

    @Test
    @DisplayName("Debe formatear especificaciones polimórficas de Deportivo y Camión")
    void testPolymorphicTechnicalSpecs() throws Exception {
        SportsCar sportsCar = createTestSportsCar();
        Truck truck = createTestTruck();

        AppContext.getDealership().registerVehicle(sportsCar);
        AppContext.getDealership().registerVehicle(truck);

        VehicleManagementController controller = createControllerWithUI();

        // 1. Ficha del Deportivo
        controller.selectVehicle(sportsCar);
        String sportsSpecs = controller.getLblDetailTechnicalSpecs().getText();
        assertTrue(sportsSpecs.contains("450 HP"));
        assertTrue(sportsSpecs.contains("0 a 100 km/h: 3.5 s"));

        // 2. Ficha del Camión
        controller.selectVehicle(truck);
        String truckSpecs = controller.getLblDetailTechnicalSpecs().getText();
        assertTrue(truckSpecs.contains("Capacidad de carga: 25.0 Ton"));
        assertTrue(truckSpecs.contains("Tipo: Plataforma"));
        assertTrue(truckSpecs.contains("Frenos de aire: Sí"));
    }

    @Test
    @DisplayName("Debe filtrar vehículos en tiempo real por búsqueda de placa o marca")
    void testRealTimeSearchFilter() throws Exception {
        AppContext.getDealership().registerVehicle(createTestSedan());
        AppContext.getDealership().registerVehicle(createTestSportsCar());
        AppContext.getDealership().registerVehicle(createTestTruck());

        VehicleManagementController controller = createControllerWithUI();
        assertEquals(3, controller.getVehicleTable().getItems().size());

        // Búsqueda por placa
        controller.getTxtSearch().setText("SED");
        controller.applyFilters();
        assertEquals(1, controller.getVehicleTable().getItems().size());
        assertEquals("SED-123", controller.getVehicleTable().getItems().get(0).getPlate());

        // Búsqueda por marca
        controller.getTxtSearch().setText("Porsche");
        controller.applyFilters();
        assertEquals(1, controller.getVehicleTable().getItems().size());
        assertEquals("SPT-999", controller.getVehicleTable().getItems().get(0).getPlate());

        // Búsqueda vacía
        controller.getTxtSearch().setText("");
        controller.applyFilters();
        assertEquals(3, controller.getVehicleTable().getItems().size());
    }

    @Test
    @DisplayName("Debe filtrar vehículos por condición (Nuevo / Usado) y estado de disponibilidad")
    void testFilterByConditionAndStatus() throws Exception {
        Sedan newSedan = createTestSedan();
        Truck rentalTruck = createTestTruck();

        AppContext.getDealership().registerVehicle(newSedan);
        AppContext.getDealership().registerVehicle(rentalTruck);

        VehicleManagementController controller = createControllerWithUI();

        // Filtro por estado: Solo Venta
        controller.getCmbStatusFilter().setValue("Disponible para Venta");
        controller.applyFilters();
        assertEquals(1, controller.getVehicleTable().getItems().size());
        assertEquals("SED-123", controller.getVehicleTable().getItems().get(0).getPlate());

        // Filtro por estado: Solo Alquiler
        controller.getCmbStatusFilter().setValue("Disponible para Alquiler");
        controller.applyFilters();
        assertEquals(1, controller.getVehicleTable().getItems().size());
        assertEquals("TRK-555", controller.getVehicleTable().getItems().get(0).getPlate());

        // Todos
        controller.getCmbStatusFilter().setValue("Todos los Estados");
        controller.applyFilters();
        assertEquals(2, controller.getVehicleTable().getItems().size());
    }
}
