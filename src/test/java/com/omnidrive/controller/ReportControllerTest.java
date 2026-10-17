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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Controlador de Reportes Gerenciales y Comisiones (T12 - Spec 006)")
class ReportControllerTest {

    private Employee employee;
    private Customer customer;
    private Sedan sedan;

    @BeforeAll
    static void initJavaFx() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // JavaFX toolkit ya inicializado
        }
    }

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws Exception {
        AppContext.reset();
        AppContext.init(tempDir.resolve("dealership.dat"));

        employee = new Employee("EMP-REP-01", "Carlos", "Vives", "3005551234", "cvives@omnidrive.com", "cvives", "Secret123*");
        AppContext.getDealership().registerEmployee(employee);
        AppContext.login(employee);

        customer = new Customer("CUST-REP-01", "Shakira", "Mebarak", "3104445678", "shakira@gmail.com");
        AppContext.getDealership().registerCustomer(customer);

        sedan = new Sedan("REP-101", "Toyota Corolla", 2024, 6, 220.0, 2000.0,
                FuelType.GASOLINE, TransmissionType.AUTOMATIC, VehicleCondition.NEW,
                VehicleStatus.AVAILABLE_FOR_SALE, 50_000.0, null,
                true, null, null,
                5, 4, 6, true, true, true,
                450.0, true, true, true, true);
        AppContext.getDealership().registerVehicle(sedan);
    }

    private ReportController createControllerWithUI() {
        ReportController controller = new ReportController();

        controller.setCmbEmployee(new ComboBox<>());
        controller.setDpStartDate(new DatePicker());
        controller.setDpEndDate(new DatePicker());
        controller.setBtnGenerateReport(new Button());
        controller.setBtnExportTxt(new Button());

        controller.setLblPurchaseAmount(new Label());
        controller.setLblSaleAmount(new Label());
        controller.setLblRentalAmount(new Label());
        controller.setLblSalesCommission(new Label());
        controller.setLblGrandTotal(new Label());
        controller.setLblTotalTransactions(new Label());

        controller.setTblReportTransactions(new TableView<>());
        controller.setColTxId(new TableColumn<>());
        controller.setColTxType(new TableColumn<>());
        controller.setColTxDate(new TableColumn<>());
        controller.setColTxVehicle(new TableColumn<>());
        controller.setColTxCustomer(new TableColumn<>());
        controller.setColTxAmount(new TableColumn<>());

        controller.initialize();
        return controller;
    }

    @Test
    @DisplayName("Inicialización del controlador carga la lista de empleados")
    void testInitializePopulatesEmployees() {
        ReportController controller = createControllerWithUI();

        assertNotNull(controller.getCmbEmployee().getItems());
        assertFalse(controller.getCmbEmployee().getItems().isEmpty());
        assertTrue(controller.getCmbEmployee().getItems().stream()
                .anyMatch(emp -> emp.getId().equals(employee.getId())));
    }

    @Test
    @DisplayName("Generación de reporte calcula el 2% de comisión sobre ventas y actualiza métricas")
    void testGenerateReportSuccessWithCommission() throws Exception {
        // Registrar una venta de $50,000 realizada por el empleado
        AppContext.getDealership().sellVehicle(employee, customer, sedan.getPlate(), 50000.0, Instant.now());

        ReportController controller = createControllerWithUI();
        controller.getCmbEmployee().setValue(employee);
        controller.getDpStartDate().setValue(LocalDate.now().minusDays(1));
        controller.getDpEndDate().setValue(LocalDate.now().plusDays(1));

        controller.handleGenerateReport();

        assertNotNull(controller.getCurrentReport());
        assertEquals(1, controller.getCurrentReport().getSaleCount());
        assertEquals(50000.0, controller.getCurrentReport().getTotalSaleAmount());
        assertEquals(1000.0, controller.getCurrentReport().getSalesCommission(), 0.001); // 2% de 50,000

        assertTrue(controller.getLblSalesCommission().getText().contains("1,000.00") ||
                controller.getLblSalesCommission().getText().contains("1000.00"));
        assertEquals(1, controller.getTblReportTransactions().getItems().size());
    }

    @Test
    @DisplayName("Generación de reporte con fechas invertidas maneja el error sin colapsar")
    void testGenerateReportInvertedDatesShowsError() {
        ReportController controller = createControllerWithUI();
        controller.getCmbEmployee().setValue(employee);
        controller.getDpStartDate().setValue(LocalDate.now().plusDays(5));
        controller.getDpEndDate().setValue(LocalDate.now());

        controller.handleGenerateReport();

        assertNull(controller.getCurrentReport());
    }

    @Test
    @DisplayName("Generación de reporte sin empleado seleccionado no genera reporte")
    void testGenerateReportWithoutEmployeeShowsWarning() {
        ReportController controller = createControllerWithUI();
        controller.getCmbEmployee().setValue(null);
        controller.getDpStartDate().setValue(LocalDate.now().minusDays(1));
        controller.getDpEndDate().setValue(LocalDate.now());

        controller.handleGenerateReport();

        assertNull(controller.getCurrentReport());
    }

    @Test
    @DisplayName("Exportación de reporte a TXT escribe correctamente el desglose auditado con el 2% de comisión")
    void testExportReportToTextFile(@TempDir Path tempDir) throws Exception {
        AppContext.getDealership().sellVehicle(employee, customer, sedan.getPlate(), 80000.0, Instant.now());

        ReportController controller = createControllerWithUI();
        controller.getCmbEmployee().setValue(employee);
        controller.getDpStartDate().setValue(LocalDate.now().minusDays(2));
        controller.getDpEndDate().setValue(LocalDate.now().plusDays(2));
        controller.handleGenerateReport();

        File exportFile = tempDir.resolve("reporte_test.txt").toFile();
        boolean success = controller.exportReportToFile(exportFile);

        assertTrue(success);
        assertTrue(exportFile.exists());

        String content = Files.readString(exportFile.toPath());
        assertTrue(content.contains("OMNIDRIVE"));
        assertTrue(content.contains("REPORTE GERENCIAL"));
        assertTrue(content.contains("Carlos Vives"));
        assertTrue(content.contains("EMP-REP-01"));
        assertTrue(content.contains("2%"));
        assertTrue(content.contains("1,600.00") || content.contains("1600.00")); // 2% de 80,000
        assertTrue(content.contains("REP-101"));
    }
}
