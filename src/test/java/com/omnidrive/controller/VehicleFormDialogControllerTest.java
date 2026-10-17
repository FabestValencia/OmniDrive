package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.model.Sedan;
import com.omnidrive.model.Truck;
import com.omnidrive.model.Vehicle;
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

@DisplayName("Pruebas Unitarias - Controlador de Formulario Modal de Vehículos (T9 - Spec 006)")
class VehicleFormDialogControllerTest {

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

    private VehicleFormDialogController createControllerWithUI() {
        VehicleFormDialogController controller = new VehicleFormDialogController();

        controller.setCmbVehicleType(new ComboBox<>());
        controller.setTxtPlate(new TextField());
        controller.setTxtBrand(new TextField());
        controller.setTxtModelYear(new TextField());
        controller.setTxtSpeedCount(new TextField());
        controller.setTxtMaxSpeed(new TextField());
        controller.setTxtCylinderCapacity(new TextField());
        controller.setCmbTransmission(new ComboBox<>());
        controller.setCmbFuelType(new ComboBox<>());
        controller.setCmbCondition(new ComboBox<>());
        controller.setCmbStatus(new ComboBox<>());
        controller.setTxtBasePrice(new TextField());
        controller.setTxtPhotoUrl(new TextField());
        controller.setChkTechnicalReview(new CheckBox());
        controller.setImgPreview(new ImageView());

        // Paneles de Combustible
        controller.setPaneElectric(new VBox());
        controller.setTxtAutonomyKm(new TextField());
        controller.setTxtChargingHours(new TextField());

        controller.setPaneHybrid(new VBox());
        controller.setChkPlugIn(new CheckBox());
        controller.setChkMildHybrid(new CheckBox());

        // Paneles por Tipo de Vehículo
        controller.setPanePassenger(new VBox());
        controller.setTxtPassengerCount(new TextField());
        controller.setTxtDoorsCount(new TextField());
        controller.setTxtAirbagsCount(new TextField());
        controller.setChkAirConditioning(new CheckBox());
        controller.setChkReverseCamera(new CheckBox());
        controller.setChkAbs(new CheckBox());

        controller.setPaneTrunk(new VBox());
        controller.setTxtTrunkCapacity(new TextField());

        controller.setPaneSedanSuv(new VBox());
        controller.setChkCruiseControl(new CheckBox());
        controller.setChkCollisionSensors(new CheckBox());
        controller.setChkCrossTraffic(new CheckBox());
        controller.setChkLaneAssist(new CheckBox());

        controller.setPaneFourByFour(new VBox());
        controller.setChkFourByFour(new CheckBox());

        controller.setPanePickUp(new VBox());
        controller.setTxtBedCapacity(new TextField());

        controller.setPaneSportsCar(new VBox());
        controller.setTxtHorsepower(new TextField());
        controller.setTxtZeroToHundred(new TextField());

        controller.setPaneBus(new VBox());
        controller.setTxtAxleCount(new TextField());
        controller.setTxtEmergencyExits(new TextField());

        controller.setPaneTruck(new VBox());
        controller.setTxtLoadCapacityTons(new TextField());
        controller.setChkTruckAirBrakes(new CheckBox());
        controller.setChkTruckAirConditioning(new CheckBox());
        controller.setChkTruckAbs(new CheckBox());
        controller.setTxtTruckAxles(new TextField());
        controller.setCmbTruckType(new ComboBox<>());

        controller.setBtnSave(new Button());
        controller.setBtnCancel(new Button());

        controller.initialize();
        return controller;
    }

    @Test
    @DisplayName("Debe conmutar reactivamente los paneles al cambiar el tipo de vehículo")
    void testReactivePanelsByVehicleType() {
        VehicleFormDialogController controller = createControllerWithUI();

        // 1. Sedán
        controller.getCmbVehicleType().setValue("Sedán");
        controller.updateFormVisibility();
        assertTrue(controller.getPanePassenger().isVisible(), "Sedán debe ver panel de pasajeros");
        assertTrue(controller.getPaneTrunk().isVisible(), "Sedán debe ver panel de maletero");
        assertTrue(controller.getPaneSedanSuv().isVisible(), "Sedán debe ver panel de asistentes");
        assertFalse(controller.getPaneTruck().isVisible(), "Sedán no debe ver panel de camión");
        assertFalse(controller.getPaneSportsCar().isVisible());

        // 2. Camión
        controller.getCmbVehicleType().setValue("Camión");
        controller.updateFormVisibility();
        assertTrue(controller.getPaneTruck().isVisible(), "Camión debe ver panel de camión");
        assertFalse(controller.getPanePassenger().isVisible(), "Camión no usa panel de pasajeros regular");
        assertFalse(controller.getPaneTrunk().isVisible());

        // 3. Moto
        controller.getCmbVehicleType().setValue("Moto");
        controller.updateFormVisibility();
        assertFalse(controller.getPanePassenger().isVisible());
        assertFalse(controller.getPaneTruck().isVisible());
        assertFalse(controller.getPaneSportsCar().isVisible());
    }

    @Test
    @DisplayName("Debe mostrar paneles ecológicos al seleccionar combustible Eléctrico o Híbrido")
    void testReactivePanelsByFuelType() {
        VehicleFormDialogController controller = createControllerWithUI();

        // 1. Gasolina (ningún panel eco)
        controller.getCmbFuelType().setValue(FuelType.GASOLINE);
        controller.updateFormVisibility();
        assertFalse(controller.getPaneElectric().isVisible());
        assertFalse(controller.getPaneHybrid().isVisible());

        // 2. Eléctrico
        controller.getCmbFuelType().setValue(FuelType.ELECTRIC);
        controller.updateFormVisibility();
        assertTrue(controller.getPaneElectric().isVisible(), "Debe ver panel eléctrico");
        assertFalse(controller.getPaneHybrid().isVisible());

        // 3. Híbrido
        controller.getCmbFuelType().setValue(FuelType.HYBRID);
        controller.updateFormVisibility();
        assertFalse(controller.getPaneElectric().isVisible());
        assertTrue(controller.getPaneHybrid().isVisible(), "Debe ver panel híbrido");
    }

    @Test
    @DisplayName("Debe registrar exitosamente un Sedán y persistirlo en AppContext")
    void testRegisterSedanSuccess() {
        VehicleFormDialogController controller = createControllerWithUI();

        controller.getCmbVehicleType().setValue("Sedán");
        controller.getTxtPlate().setText("XYZ-789");
        controller.getTxtBrand().setText("Mazda 3");
        controller.getTxtModelYear().setText("2024");
        controller.getTxtSpeedCount().setText("6");
        controller.getTxtMaxSpeed().setText("215");
        controller.getTxtCylinderCapacity().setText("2000");
        controller.getCmbTransmission().setValue(TransmissionType.AUTOMATIC);
        controller.getCmbFuelType().setValue(FuelType.GASOLINE);
        controller.getCmbCondition().setValue(VehicleCondition.NEW);
        controller.getCmbStatus().setValue(VehicleStatus.AVAILABLE_FOR_SALE);
        controller.getTxtBasePrice().setText("105000000");

        // Datos de Pasajero y Sedán
        controller.getTxtPassengerCount().setText("5");
        controller.getTxtDoorsCount().setText("4");
        controller.getTxtAirbagsCount().setText("6");
        controller.getChkAirConditioning().setSelected(true);
        controller.getChkReverseCamera().setSelected(true);
        controller.getChkAbs().setSelected(true);
        controller.getTxtTrunkCapacity().setText("440");
        controller.getChkCruiseControl().setSelected(true);
        controller.getChkCollisionSensors().setSelected(true);
        controller.getChkCrossTraffic().setSelected(false);
        controller.getChkLaneAssist().setSelected(true);

        Vehicle created = controller.processSaveVehicle();

        assertNotNull(created, "El vehículo debe registrarse con éxito");
        assertInstanceOf(Sedan.class, created);
        assertEquals(1, AppContext.getDealership().getVehicleCount());
        assertEquals("XYZ-789", AppContext.getDealership().findVehicleByPlate("XYZ-789").getPlate());
    }

    @Test
    @DisplayName("Debe bloquear el registro de un vehículo Usado sin revisión técnica aprobada (RF-14, RF-18)")
    void testRejectUsedVehicleWithoutTechnicalReview() {
        VehicleFormDialogController controller = createControllerWithUI();

        controller.getCmbVehicleType().setValue("Sedán");
        controller.getTxtPlate().setText("OLD-111");
        controller.getTxtBrand().setText("Chevrolet Optra");
        controller.getTxtModelYear().setText("2010");
        controller.getTxtSpeedCount().setText("5");
        controller.getTxtMaxSpeed().setText("180");
        controller.getTxtCylinderCapacity().setText("1600");
        controller.getCmbTransmission().setValue(TransmissionType.MANUAL);
        controller.getCmbFuelType().setValue(FuelType.GASOLINE);
        controller.getCmbCondition().setValue(VehicleCondition.USED); // USADO
        controller.getCmbStatus().setValue(VehicleStatus.AVAILABLE_FOR_SALE);
        controller.getTxtBasePrice().setText("25000000");
        controller.getChkTechnicalReview().setSelected(false); // NO APROBADA

        controller.getTxtPassengerCount().setText("5");
        controller.getTxtDoorsCount().setText("4");
        controller.getTxtAirbagsCount().setText("2");
        controller.getTxtTrunkCapacity().setText("400");

        Vehicle created = controller.processSaveVehicle();

        assertNull(created, "Debe rechazar el vehículo usado sin revisión técnica");
        assertEquals(0, AppContext.getDealership().getVehicleCount(), "No debe registrarse en el concesionario");
    }

    @Test
    @DisplayName("Debe registrar un Camión con atributos especializados")
    void testRegisterTruckSuccess() {
        VehicleFormDialogController controller = createControllerWithUI();

        controller.getCmbVehicleType().setValue("Camión");
        controller.getTxtPlate().setText("TRK-777");
        controller.getTxtBrand().setText("Hino Serie 500");
        controller.getTxtModelYear().setText("2023");
        controller.getTxtSpeedCount().setText("8");
        controller.getTxtMaxSpeed().setText("120");
        controller.getTxtCylinderCapacity().setText("7684");
        controller.getCmbTransmission().setValue(TransmissionType.MANUAL);
        controller.getCmbFuelType().setValue(FuelType.DIESEL);
        controller.getCmbCondition().setValue(VehicleCondition.NEW);
        controller.getCmbStatus().setValue(VehicleStatus.AVAILABLE_FOR_RENT);
        controller.getTxtBasePrice().setText("12000000");

        // Datos especializados de Camión
        controller.getTxtLoadCapacityTons().setText("15.5");
        controller.getChkTruckAirConditioning().setSelected(true);
        controller.getChkTruckAirBrakes().setSelected(true);
        controller.getChkTruckAbs().setSelected(true);
        controller.getTxtTruckAxles().setText("3");
        controller.getCmbTruckType().setValue(TruckType.BOX_TRUCK);

        Vehicle created = controller.processSaveVehicle();

        assertNotNull(created);
        assertInstanceOf(Truck.class, created);
        Truck trk = (Truck) created;
        assertEquals(15.5, trk.getLoadCapacityTons());
        assertEquals(TruckType.BOX_TRUCK, trk.getTruckType());
        assertEquals(1, AppContext.getDealership().getVehicleCount());
    }

    @Test
    @DisplayName("Debe aplicar clase de error visual y rechazar registro con datos numéricos o placa inválida")
    void testValidationFailureHighlightsFields() {
        VehicleFormDialogController controller = createControllerWithUI();

        controller.getCmbVehicleType().setValue("Sedán");
        controller.getTxtPlate().setText("INVALID_PLATE");
        controller.getTxtBrand().setText("Mazda");
        controller.getTxtModelYear().setText("ABCD");
        controller.getTxtSpeedCount().setText("6");
        controller.getTxtMaxSpeed().setText("-200");
        controller.getTxtCylinderCapacity().setText("2000");
        controller.getTxtBasePrice().setText("-1000");

        Vehicle created = controller.processSaveVehicle();

        assertNull(created, "No debe crear vehículo con datos inválidos");
        assertTrue(controller.getTxtPlate().getStyleClass().contains("text-field-error"),
                "Debe marcar txtPlate con text-field-error");
        assertTrue(controller.getTxtModelYear().getStyleClass().contains("text-field-error"),
                "Debe marcar txtModelYear con text-field-error");
        assertTrue(controller.getTxtMaxSpeed().getStyleClass().contains("text-field-error"),
                "Debe marcar txtMaxSpeed con text-field-error");
        assertTrue(controller.getTxtBasePrice().getStyleClass().contains("text-field-error"),
                "Debe marcar txtBasePrice con text-field-error");
        assertEquals(0, AppContext.getDealership().getVehicleCount());
    }
}
