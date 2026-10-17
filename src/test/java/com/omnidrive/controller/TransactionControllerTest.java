package com.omnidrive.controller;

import com.omnidrive.context.AppContext;
import com.omnidrive.model.*;
import com.omnidrive.model.enums.*;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Controlador de Transacciones Comerciales (T11 - Spec 006)")
class TransactionControllerTest {

    private Employee employee;
    private Customer customer;

    @BeforeAll
    static void initJavaFx() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit JavaFX ya iniciado
        }
    }

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws Exception {
        AppContext.reset();
        AppContext.init(tempDir.resolve("dealership.dat"));

        employee = new Employee("EMP-01", "Andrés", "Cepeda", "3001234567", "andres@omnidrive.com", "acepeda", "Pass123*");
        AppContext.getDealership().registerEmployee(employee);
        AppContext.login(employee);

        customer = new Customer("CUST-01", "María", "José", "3109876543", "mariajose@gmail.com");
        AppContext.getDealership().registerCustomer(customer);
    }

    private TransactionController createControllerWithUI() {
        TransactionController controller = new TransactionController();

        // Pestaña Compra
        controller.setCmbBuyEmployee(new ComboBox<>());
        controller.setCmbBuyCustomer(new ComboBox<>());
        controller.setCmbBuyVehicleType(new ComboBox<>());
        controller.setTxtBuyPlate(new TextField());
        controller.setTxtBuyBrand(new TextField());
        controller.setTxtBuyYear(new TextField());
        controller.setTxtBuyCylinder(new TextField());
        controller.setCmbBuyFuel(new ComboBox<>());
        controller.setCmbBuyTransmission(new ComboBox<>());
        controller.setCmbBuyCondition(new ComboBox<>());
        controller.setChkBuyTechnicalReview(new CheckBox());
        controller.setCmbBuyInitialStatus(new ComboBox<>());
        controller.setTxtBuyAmount(new TextField());
        controller.setBtnProcessBuy(new Button());

        // Pestaña Venta
        controller.setCmbSaleEmployee(new ComboBox<>());
        controller.setCmbSaleCustomer(new ComboBox<>());
        controller.setCmbSaleVehicle(new ComboBox<>());
        controller.setTxtSaleAmount(new TextField());
        controller.setBtnProcessSale(new Button());

        // Pestaña Alquiler
        controller.setCmbRentalEmployee(new ComboBox<>());
        controller.setCmbRentalCustomer(new ComboBox<>());
        controller.setCmbRentalVehicle(new ComboBox<>());
        controller.setDpRentalStart(new DatePicker());
        controller.setDpRentalEnd(new DatePicker());
        controller.setTxtRentalDays(new TextField());
        controller.setBtnProcessRental(new Button());

        // Pestaña Devolución
        controller.setCmbReturnVehicle(new ComboBox<>());
        controller.setBtnProcessReturn(new Button());

        // Pestaña Historial
        controller.setHistoryTable(new TableView<>());
        controller.setColTxnId(new TableColumn<>());
        controller.setColTxnType(new TableColumn<>());
        controller.setColTxnDate(new TableColumn<>());
        controller.setColTxnEmployee(new TableColumn<>());
        controller.setColTxnCustomer(new TableColumn<>());
        controller.setColTxnVehicle(new TableColumn<>());
        controller.setColTxnAmount(new TableColumn<>());

        controller.initialize();
        return controller;
    }

    private Sedan createSaleVehicle(String plate) throws Exception {
        return new Sedan(plate, "Renault Logan", 2024, 5, 175.0, 1600.0,
                FuelType.GASOLINE, TransmissionType.MANUAL, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 60_000_000.0, null,
                true, null, null,
                5, 4, 4, true, true, true,
                510.0, false, false, false, false);
    }

    private Sedan createRentalVehicle(String plate) throws Exception {
        return new Sedan(plate, "Nissan Versa", 2024, 6, 185.0, 1600.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_RENT, 120_000.0, null,
                true, null, null,
                5, 4, 6, true, true, true,
                480.0, true, true, true, true);
    }

    @Test
    @DisplayName("Debe procesar venta exitosa conmutando vehículo a SOLD y registrando transacción (RF-10)")
    void testProcessSaleSuccess() throws Exception {
        Sedan vehicle = createSaleVehicle("VNT-100");
        AppContext.getDealership().registerVehicle(vehicle);

        TransactionController controller = createControllerWithUI();

        controller.getCmbSaleEmployee().setValue(employee);
        controller.getCmbSaleCustomer().setValue(customer);
        controller.getCmbSaleVehicle().setValue(vehicle);
        controller.getTxtSaleAmount().setText("60000000");

        boolean result = controller.processSale();

        assertTrue(result, "La venta debe registrarse con éxito");
        assertEquals(VehicleStatus.SOLD, vehicle.getStatus(), "El vehículo debe conmutar a SOLD");
        assertEquals(1, AppContext.getDealership().getAllTransactions().size());
        assertEquals(1, controller.getHistoryTable().getItems().size());
    }

    @Test
    @DisplayName("Debe procesar alquiler exitoso conmutando vehículo a RENTED (RF-10)")
    void testProcessRentalSuccess() throws Exception {
        Sedan vehicle = createRentalVehicle("RNT-200");
        AppContext.getDealership().registerVehicle(vehicle);

        TransactionController controller = createControllerWithUI();

        controller.getCmbRentalEmployee().setValue(employee);
        controller.getCmbRentalCustomer().setValue(customer);
        controller.getCmbRentalVehicle().setValue(vehicle);
        controller.getDpRentalStart().setValue(LocalDate.now());
        controller.getDpRentalEnd().setValue(LocalDate.now().plusDays(3));
        controller.getTxtRentalDays().setText("3");

        boolean result = controller.processRental();

        assertTrue(result, "El alquiler debe registrarse con éxito");
        assertEquals(VehicleStatus.RENTED, vehicle.getStatus(), "El vehículo debe pasar a RENTED");
        assertEquals(1, AppContext.getDealership().getAllTransactions().size());
    }

    @Test
    @DisplayName("Debe procesar devolución de vehículo alquilado retornándolo a AVAILABLE_FOR_RENT")
    void testProcessReturnSuccess() throws Exception {
        Sedan vehicle = createRentalVehicle("RNT-300");
        AppContext.getDealership().registerVehicle(vehicle);
        AppContext.getDealership().rentVehicle(employee, customer, "RNT-300", LocalDate.now(), LocalDate.now().plusDays(2), 2, null);
        assertEquals(VehicleStatus.RENTED, vehicle.getStatus());

        TransactionController controller = createControllerWithUI();
        controller.getCmbReturnVehicle().setValue(vehicle);

        boolean result = controller.processReturn();

        assertTrue(result, "La devolución debe registrarse exitosamente");
        assertEquals(VehicleStatus.AVAILABLE_FOR_RENT, vehicle.getStatus(), "Debe volver a AVAILABLE_FOR_RENT");
    }

    @Test
    @DisplayName("Debe bloquear compra de vehículo usado sin revisión técnica aprobada (RF-09, Escenario 5)")
    void testRejectUsedVehiclePurchaseWithoutTechnicalReview() {
        TransactionController controller = createControllerWithUI();

        controller.getCmbBuyEmployee().setValue(employee);
        controller.getCmbBuyCustomer().setValue(customer);
        controller.getCmbBuyVehicleType().setValue("Sedán");
        controller.getTxtBuyPlate().setText("USD-555");
        controller.getTxtBuyBrand().setText("Chevrolet Spark");
        controller.getTxtBuyYear().setText("2015");
        controller.getTxtBuyCylinder().setText("1200");
        controller.getCmbBuyFuel().setValue(FuelType.GASOLINE);
        controller.getCmbBuyTransmission().setValue(TransmissionType.MANUAL);
        controller.getCmbBuyCondition().setValue(VehicleCondition.USED); // USADO
        controller.getChkBuyTechnicalReview().setSelected(false); // NO REVISADO
        controller.getCmbBuyInitialStatus().setValue(VehicleStatus.AVAILABLE_FOR_SALE);
        controller.getTxtBuyAmount().setText("18000000");

        boolean result = controller.processBuy();

        assertFalse(result, "La compra debe ser rechazada por falta de revisión técnica");
        assertNull(AppContext.getDealership().findVehicleByPlate("USD-555"), "El vehículo no debe ser ingresado");
        assertEquals(0, AppContext.getDealership().getAllTransactions().size());
    }

    @Test
    @DisplayName("Debe procesar compra de vehículo usado si tiene la revisión técnica aprobada")
    void testAcceptUsedVehiclePurchaseWithTechnicalReview() {
        TransactionController controller = createControllerWithUI();

        controller.getCmbBuyEmployee().setValue(employee);
        controller.getCmbBuyCustomer().setValue(customer);
        controller.getCmbBuyVehicleType().setValue("Sedán");
        controller.getTxtBuyPlate().setText("USD-888");
        controller.getTxtBuyBrand().setText("Toyota Yaris");
        controller.getTxtBuyYear().setText("2018");
        controller.getTxtBuyCylinder().setText("1500");
        controller.getCmbBuyFuel().setValue(FuelType.GASOLINE);
        controller.getCmbBuyTransmission().setValue(TransmissionType.AUTOMATIC);
        controller.getCmbBuyCondition().setValue(VehicleCondition.USED);
        controller.getChkBuyTechnicalReview().setSelected(true); // APROBADA
        controller.getCmbBuyInitialStatus().setValue(VehicleStatus.AVAILABLE_FOR_SALE);
        controller.getTxtBuyAmount().setText("42000000");

        boolean result = controller.processBuy();

        assertTrue(result, "La compra debe ser aprobada");
        assertNotNull(AppContext.getDealership().findVehicleByPlate("USD-888"), "El vehículo debe registrarse");
        assertEquals(1, AppContext.getDealership().getAllTransactions().size());
    }
}
